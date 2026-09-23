package com.worklog.tasklog;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worklog.common.exception.BusinessException;
import com.worklog.common.exception.ErrorCode;
import com.worklog.dailylog.DailyLog;
import com.worklog.dailylog.DailyLogRepository;
import com.worklog.member.Member;
import com.worklog.member.MemberRepository;
import com.worklog.task.Task;
import com.worklog.task.TaskRepository;
import com.worklog.tasklog.dto.TaskLogRequest;
import com.worklog.tasklog.dto.TaskLogResponse;
import com.worklog.tasklog.dto.TaskLogUpdateRequest;

@Service
public class TaskLogService {

	private final TaskLogRepository taskLogRepository;
	private final DailyLogRepository dailyLogRepository;
	private final TaskRepository taskRepository;
	private final MemberRepository memberRepository;

	public TaskLogService(TaskLogRepository taskLogRepository, DailyLogRepository dailyLogRepository,
			TaskRepository taskRepository, MemberRepository memberRepository) {
		this.taskLogRepository = taskLogRepository;
		this.dailyLogRepository = dailyLogRepository;
		this.taskRepository = taskRepository;
		this.memberRepository = memberRepository;
	}

	// 진행 메모 등록. 그 날짜의 일일 기록이 아직 없으면 회고 없이(summary=null) 먼저 만들어둔다 —
	// 회고보다 업무 메모를 먼저 남기는 사용 흐름을 막지 않으려고.
	@Transactional
	public TaskLogResponse createTaskLog(Long memberId, LocalDate date, TaskLogRequest request) {
		DailyLog dailyLog = findOrCreateDailyLog(memberId, date);
		Task task = findMyTask(request.taskId(), memberId);

		TaskLog saved = taskLogRepository.save(new TaskLog(dailyLog, task, request.content(), request.spentMinutes()));

		return TaskLogResponse.from(saved);
	}

	@Transactional
	public TaskLogResponse updateTaskLog(Long id, Long memberId, TaskLogUpdateRequest request) {
		TaskLog taskLog = findMyTaskLog(id, memberId);
		taskLog.update(request.content(), request.spentMinutes());

		return TaskLogResponse.from(taskLog);
	}

	@Transactional
	public void deleteTaskLog(Long id, Long memberId) {
		TaskLog taskLog = findMyTaskLog(id, memberId);
		taskLogRepository.delete(taskLog);
	}

	// 업무별 진행 메모 조회 (시간순)
	public List<TaskLogResponse> getTaskLogsByTask(Long taskId, Long memberId) {
		findMyTask(taskId, memberId);

		return taskLogRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
				.map(TaskLogResponse::from)
				.toList();
	}

	private DailyLog findOrCreateDailyLog(Long memberId, LocalDate date) {
		return dailyLogRepository.findByMemberIdAndLogDate(memberId, date)
				.orElseGet(() -> {
					Member member = memberRepository.findById(memberId)
							.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
					return dailyLogRepository.save(new DailyLog(member, date, null));
				});
	}

	// TaskService에 있는 것과 같은 확인 로직 (업무가 존재하고, 내 것인지 확인)
	private Task findMyTask(Long taskId, Long memberId) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

		if (!task.getMember().getId().equals(memberId)) {
			throw new BusinessException(ErrorCode.TASK_ACCESS_DENIED);
		}

		return task;
	}

	// TaskLog는 memberId를 직접 갖고 있지 않아서, 연결된 업무의 주인을 통해 확인한다
	private TaskLog findMyTaskLog(Long id, Long memberId) {
		TaskLog taskLog = taskLogRepository.findById(id)
				.orElseThrow(() -> new BusinessException(ErrorCode.TASK_LOG_NOT_FOUND));

		if (!taskLog.getTask().getMember().getId().equals(memberId)) {
			throw new BusinessException(ErrorCode.TASK_LOG_ACCESS_DENIED);
		}

		return taskLog;
	}

}
