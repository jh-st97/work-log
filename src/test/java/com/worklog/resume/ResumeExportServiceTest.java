package com.worklog.resume;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.worklog.common.exception.BusinessException;
import com.worklog.common.exception.ErrorCode;
import com.worklog.support.ServiceTestSupport;
import com.worklog.tag.Tag;
import com.worklog.tag.TagRepository;
import com.worklog.task.TaskPriority;
import com.worklog.task.TaskResultService;
import com.worklog.task.TaskStatus;
import com.worklog.task.dto.TaskRequest;
import com.worklog.task.dto.TaskResponse;
import com.worklog.task.dto.TaskResultRequest;
import com.worklog.task.dto.TaskStatusRequest;
import com.worklog.tasklog.TaskLogService;
import com.worklog.tasklog.dto.TaskLogRequest;

class ResumeExportServiceTest extends ServiceTestSupport {

	@Autowired
	private ResumeExportService resumeExportService;
	@Autowired
	private TagRepository tagRepository;
	@Autowired
	private TaskResultService taskResultService;
	@Autowired
	private TaskLogService taskLogService;

	@Test
	@DisplayName("업무의 기본 정보·기술·성과·진행 과정이 마크다운에 담긴다")
	void exportResume_containsTaskDetails() {
		Tag spring = tagRepository.save(new Tag(owner, "Spring Boot"));
		TaskResponse task = taskService.createTask(owner.getId(), new TaskRequest("배포 자동화", "수동 배포를 스크립트로 바꿨다",
				TaskPriority.MEDIUM, null, project.getId(), List.of(spring.getId()), List.of()));
		taskService.changeStatus(task.id(), owner.getId(), new TaskStatusRequest(TaskStatus.DONE));
		taskResultService.createResult(task.id(), owner.getId(), new TaskResultRequest("배포 시간", "30분", "5분"));
		taskResultService.createResult(task.id(), owner.getId(), new TaskResultRequest("배포 방식", null, "자동 배포"));
		taskLogService.createTaskLog(owner.getId(), LocalDate.of(2026, 9, 23),
				new TaskLogRequest(task.id(), "서버 접속이 막혀서\n키 방식으로 바꿔 해결", 90));

		String markdown = resumeExportService.exportResume(owner.getId(), List.of(task.id())).markdown();

		assertThat(markdown).contains("# 업무 경력 정리");
		assertThat(markdown).contains("## 배포 자동화");
		assertThat(markdown).contains("- **프로젝트**: 내 프로젝트");
		assertThat(markdown).contains("- **상태**: 완료");
		assertThat(markdown).contains("- **사용 기술**: Spring Boot");
		assertThat(markdown).contains("수동 배포를 스크립트로 바꿨다");
		// 개선 전 값이 있으면 "전 → 후", 없으면 개선 후 값만
		assertThat(markdown).contains("- 배포 시간: 30분 → 5분");
		assertThat(markdown).contains("- 배포 방식: 자동 배포");
		// 진행 메모: 날짜·소요 시간이 붙고, 여러 줄 메모는 들여쓰기로 이어진다
		assertThat(markdown).contains("- **2026-09-23** (90분): 서버 접속이 막혀서\n  키 방식으로 바꿔 해결");
	}

	@Test
	@DisplayName("태그·성과·진행 메모가 없는 업무는 해당 소제목을 만들지 않는다")
	void exportResume_omitsEmptySections() {
		TaskResponse task = createTask("단순 업무");

		String markdown = resumeExportService.exportResume(owner.getId(), List.of(task.id())).markdown();

		assertThat(markdown).contains("## 단순 업무");
		assertThat(markdown).contains("진행 중");
		assertThat(markdown).doesNotContain("사용 기술").doesNotContain("### 성과").doesNotContain("### 진행 과정");
	}

	@Test
	@DisplayName("고른 순서대로 내보내고, 같은 업무를 두 번 골라도 한 번만 나온다")
	void exportResume_keepsOrderAndDeduplicates() {
		TaskResponse first = createTask("첫 번째 업무");
		TaskResponse second = createTask("두 번째 업무");

		String markdown = resumeExportService.exportResume(owner.getId(), List.of(second.id(), first.id(), second.id()))
				.markdown();

		assertThat(markdown.indexOf("## 두 번째 업무")).isLessThan(markdown.indexOf("## 첫 번째 업무"));
		assertThat(markdown.split("## 두 번째 업무", -1)).hasSize(2);
	}

	@Test
	@DisplayName("남의 업무가 섞여 있으면 TASK_ACCESS_DENIED")
	void exportResume_withOtherMembersTask_throwsAccessDenied() {
		TaskResponse mine = createTask("내 업무");
		TaskResponse others = taskService.createTask(other.getId(), new TaskRequest("남의 업무", null,
				TaskPriority.MEDIUM, null, otherProject.getId(), List.of(), List.of()));

		assertThatThrownBy(() -> resumeExportService.exportResume(owner.getId(), List.of(mine.id(), others.id())))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.TASK_ACCESS_DENIED);
	}

	@Test
	@DisplayName("업무를 하나도 안 골랐으면 INVALID_INPUT")
	void exportResume_withEmptySelection_throwsInvalidInput() {
		assertThatThrownBy(() -> resumeExportService.exportResume(owner.getId(), List.of()))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_INPUT);
	}

}
