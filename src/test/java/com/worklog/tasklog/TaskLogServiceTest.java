package com.worklog.tasklog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.worklog.common.exception.BusinessException;
import com.worklog.common.exception.ErrorCode;
import com.worklog.dailylog.DailyLog;
import com.worklog.dailylog.DailyLogRepository;
import com.worklog.support.ServiceTestSupport;
import com.worklog.task.TaskPriority;
import com.worklog.task.dto.TaskRequest;
import com.worklog.task.dto.TaskResponse;
import com.worklog.tasklog.dto.TaskLogRequest;
import com.worklog.tasklog.dto.TaskLogResponse;
import com.worklog.tasklog.dto.TaskLogUpdateRequest;

class TaskLogServiceTest extends ServiceTestSupport {

	@Autowired
	private TaskLogService taskLogService;
	@Autowired
	private TaskLogRepository taskLogRepository;
	@Autowired
	private DailyLogRepository dailyLogRepository;

	private static final LocalDate DAY = LocalDate.of(2026, 9, 23);

	@Test
	@DisplayName("그 날짜의 일일 기록이 없으면 회고 없이(summary=null) 자동으로 만들고 진행 메모를 붙인다")
	void createTaskLog_createsDailyLogWhenMissing() {
		TaskResponse task = createTask("배포 자동화");

		TaskLogResponse created = taskLogService.createTaskLog(owner.getId(), DAY,
				new TaskLogRequest(task.id(), "스크립트 작성", 30));

		assertThat(created.taskTitle()).isEqualTo("배포 자동화");
		assertThat(created.logDate()).isEqualTo(DAY);
		DailyLog dailyLog = dailyLogRepository.findByMemberIdAndLogDate(owner.getId(), DAY).orElseThrow();
		assertThat(dailyLog.getSummary()).isNull();
	}

	@Test
	@DisplayName("같은 날 같은 업무에 진행 메모를 여러 개 남길 수 있고, 일일 기록은 하나만 만들어진다")
	void createTaskLog_allowsMultiplePerDay() {
		TaskResponse task = createTask("배포 자동화");

		taskLogService.createTaskLog(owner.getId(), DAY, new TaskLogRequest(task.id(), "오전 작업", 60));
		taskLogService.createTaskLog(owner.getId(), DAY, new TaskLogRequest(task.id(), "오후 작업", 90));

		assertThat(taskLogRepository.count()).isEqualTo(2);
		assertThat(dailyLogRepository.count()).isEqualTo(1);
	}

	@Test
	@DisplayName("남의 업무에는 진행 메모를 남길 수 없다")
	void createTaskLog_onOtherMembersTask_throwsAccessDenied() {
		TaskResponse othersTask = taskService.createTask(other.getId(),
				new TaskRequest("남의 업무", null, TaskPriority.MEDIUM, null, otherProject.getId(), List.of(), List.of()));

		assertThatThrownBy(() -> taskLogService.createTaskLog(owner.getId(), DAY,
				new TaskLogRequest(othersTask.id(), "몰래 메모", 10)))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.TASK_ACCESS_DENIED);
	}

	@Test
	@DisplayName("남의 진행 메모를 수정하거나 삭제하려 하면 TASK_LOG_ACCESS_DENIED가 발생한다")
	void updateAndDelete_otherMembersTaskLog_throwsAccessDenied() {
		TaskResponse task = createTask("내 업무");
		TaskLogResponse mine = taskLogService.createTaskLog(owner.getId(), DAY, new TaskLogRequest(task.id(), "내 메모", 10));

		assertThatThrownBy(() -> taskLogService.updateTaskLog(mine.id(), other.getId(),
				new TaskLogUpdateRequest("수정 시도", 5)))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.TASK_LOG_ACCESS_DENIED);

		assertThatThrownBy(() -> taskLogService.deleteTaskLog(mine.id(), other.getId()))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.TASK_LOG_ACCESS_DENIED);

		assertThat(taskLogRepository.count()).isEqualTo(1);
	}

	@Test
	@DisplayName("진행 메모를 수정하면 내용과 소요 시간이 바뀌고, 삭제하면 사라진다")
	void updateAndDelete_works() {
		TaskResponse task = createTask("내 업무");
		TaskLogResponse created = taskLogService.createTaskLog(owner.getId(), DAY, new TaskLogRequest(task.id(), "처음", 10));

		TaskLogResponse updated = taskLogService.updateTaskLog(created.id(), owner.getId(),
				new TaskLogUpdateRequest("고친 내용", 45));
		assertThat(updated.content()).isEqualTo("고친 내용");
		assertThat(updated.spentMinutes()).isEqualTo(45);

		taskLogService.deleteTaskLog(created.id(), owner.getId());
		assertThat(taskLogRepository.count()).isZero();
	}

	@Test
	@DisplayName("없는 진행 메모를 수정하면 TASK_LOG_NOT_FOUND가 발생한다")
	void update_whenNotExists_throwsNotFound() {
		assertThatThrownBy(() -> taskLogService.updateTaskLog(9999L, owner.getId(), new TaskLogUpdateRequest("x", 1)))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.TASK_LOG_NOT_FOUND);
	}

	@Test
	@DisplayName("업무별 진행 메모 조회는 시간순이고 날짜가 붙어서 나온다")
	void getTaskLogsByTask_returnsInOrderWithDates() {
		TaskResponse task = createTask("배포 자동화");
		taskLogService.createTaskLog(owner.getId(), DAY, new TaskLogRequest(task.id(), "첫 번째", 10));
		taskLogService.createTaskLog(owner.getId(), DAY.plusDays(1), new TaskLogRequest(task.id(), "두 번째", 20));

		List<TaskLogResponse> logs = taskLogService.getTaskLogsByTask(task.id(), owner.getId());

		assertThat(logs).extracting(TaskLogResponse::content).containsExactly("첫 번째", "두 번째");
		assertThat(logs).extracting(TaskLogResponse::logDate).containsExactly(DAY, DAY.plusDays(1));
	}

}
