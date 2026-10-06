package com.worklog.resume;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worklog.common.exception.BusinessException;
import com.worklog.common.exception.ErrorCode;
import com.worklog.resume.dto.ResumeExportResponse;
import com.worklog.task.Task;
import com.worklog.task.TaskRepository;
import com.worklog.task.TaskResult;
import com.worklog.task.TaskResultRepository;
import com.worklog.task.TaskStatus;
import com.worklog.task.TaskTagRepository;
import com.worklog.task.TaskWorkSystemRepository;
import com.worklog.tasklog.TaskLog;
import com.worklog.tasklog.TaskLogRepository;

// 선택한 업무들을 이력서에 옮겨 쓰기 좋은 마크다운 문서로 만든다.
// 업무마다: 제목·프로젝트·기간·상태·사용 기술·업무 시스템·설명, 그리고 성과와 진행 과정.
@Service
public class ResumeExportService {

	private final TaskRepository taskRepository;
	private final TaskTagRepository taskTagRepository;
	private final TaskWorkSystemRepository taskWorkSystemRepository;
	private final TaskResultRepository taskResultRepository;
	private final TaskLogRepository taskLogRepository;

	public ResumeExportService(TaskRepository taskRepository, TaskTagRepository taskTagRepository,
			TaskWorkSystemRepository taskWorkSystemRepository, TaskResultRepository taskResultRepository,
			TaskLogRepository taskLogRepository) {
		this.taskRepository = taskRepository;
		this.taskTagRepository = taskTagRepository;
		this.taskWorkSystemRepository = taskWorkSystemRepository;
		this.taskResultRepository = taskResultRepository;
		this.taskLogRepository = taskLogRepository;
	}

	// 지연 로딩(LAZY)인 프로젝트·태그·시스템 이름을 읽으므로 읽기 전용 트랜잭션이 필요하다
	@Transactional(readOnly = true)
	public ResumeExportResponse exportResume(Long memberId, List<Long> taskIds) {
		// 같은 업무를 두 번 골랐어도 한 번만 내보낸다 (고른 순서는 유지)
		List<Long> ids = taskIds.stream().distinct().toList();
		if (ids.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}

		StringBuilder markdown = new StringBuilder("# 업무 경력 정리\n");
		for (Long id : ids) {
			appendTask(markdown, findMyTask(id, memberId));
		}

		return new ResumeExportResponse(markdown.toString());
	}

	private void appendTask(StringBuilder md, Task task) {
		md.append("\n## ").append(task.getTitle()).append("\n\n");
		md.append("- **프로젝트**: ").append(task.getProject().getName()).append('\n');
		md.append("- **기간**: ").append(period(task)).append('\n');
		md.append("- **상태**: ").append(statusLabel(task.getStatus())).append('\n');

		List<String> tags = taskTagRepository.findByTaskId(task.getId()).stream()
				.map(taskTag -> taskTag.getTag().getName()).toList();
		if (!tags.isEmpty()) {
			md.append("- **사용 기술**: ").append(String.join(", ", tags)).append('\n');
		}

		List<String> systems = taskWorkSystemRepository.findByTaskId(task.getId()).stream()
				.map(taskWorkSystem -> taskWorkSystem.getWorkSystem().getName()).toList();
		if (!systems.isEmpty()) {
			md.append("- **업무 시스템**: ").append(String.join(", ", systems)).append('\n');
		}

		if (task.getDescription() != null && !task.getDescription().isBlank()) {
			md.append('\n').append(task.getDescription().strip()).append('\n');
		}

		List<TaskResult> results = taskResultRepository.findByTaskId(task.getId());
		if (!results.isEmpty()) {
			md.append("\n### 성과\n\n");
			for (TaskResult result : results) {
				md.append("- ").append(result.getMetricName()).append(": ");
				// 개선 전 값이 없으면(수치가 아닌 성과) 개선 후 값만 쓴다
				if (result.getBeforeValue() != null && !result.getBeforeValue().isBlank()) {
					md.append(result.getBeforeValue()).append(" → ");
				}
				md.append(result.getAfterValue()).append('\n');
			}
		}

		List<TaskLog> logs = taskLogRepository.findByTaskIdOrderByCreatedAtAsc(task.getId());
		if (!logs.isEmpty()) {
			md.append("\n### 진행 과정\n\n");
			for (TaskLog log : logs) {
				md.append("- **").append(log.getDailyLog().getLogDate()).append("**");
				if (log.getSpentMinutes() != null) {
					md.append(" (").append(log.getSpentMinutes()).append("분)");
				}
				md.append(": ").append(indentContinuation(log.getContent())).append('\n');
			}
		}
	}

	// 시작일은 업무를 등록한 날, 끝은 완료한 날(없으면 "진행 중")
	private String period(Task task) {
		String start = task.getCreatedAt().toLocalDate().toString();
		String end = task.getCompletedAt() != null ? task.getCompletedAt().toLocalDate().toString() : "진행 중";
		return start + " ~ " + end;
	}

	private String statusLabel(TaskStatus status) {
		return switch (status) {
		case TODO -> "예정";
		case IN_PROGRESS -> "진행중";
		case DONE -> "완료";
		};
	}

	// 여러 줄 메모가 목록 항목 밑에 이어지도록, 둘째 줄부터 들여쓴다
	private String indentContinuation(String content) {
		return content.strip().lines().collect(Collectors.joining("\n  "));
	}

	private Task findMyTask(Long id, Long memberId) {
		Task task = taskRepository.findById(id)
				.orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

		if (!task.getMember().getId().equals(memberId)) {
			throw new BusinessException(ErrorCode.TASK_ACCESS_DENIED);
		}

		return task;
	}

}
