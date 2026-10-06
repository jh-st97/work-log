package com.worklog.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.worklog.common.exception.BusinessException;
import com.worklog.common.exception.ErrorCode;
import com.worklog.support.ServiceTestSupport;
import com.worklog.task.dto.TaskRequest;
import com.worklog.task.dto.TaskResponse;
import com.worklog.task.dto.TaskResultRequest;
import com.worklog.task.dto.TaskResultResponse;

class TaskResultServiceTest extends ServiceTestSupport {

	@Autowired
	private TaskResultService taskResultService;

	@Test
	@DisplayName("성과 항목을 등록·조회·수정·삭제할 수 있다 (개선 전 값은 없어도 된다)")
	void crud_works() {
		TaskResponse task = createTask("배포 자동화");

		TaskResultResponse withBefore = taskResultService.createResult(task.id(), owner.getId(),
				new TaskResultRequest("배포 시간", "30분", "5분"));
		taskResultService.createResult(task.id(), owner.getId(), new TaskResultRequest("배포 방식", null, "자동 배포"));

		List<TaskResultResponse> results = taskResultService.getResults(task.id(), owner.getId());
		assertThat(results).hasSize(2);
		assertThat(results).extracting(TaskResultResponse::beforeValue).containsExactlyInAnyOrder("30분", null);

		TaskResultResponse updated = taskResultService.updateResult(task.id(), withBefore.id(), owner.getId(),
				new TaskResultRequest("배포 시간", "30분", "3분"));
		assertThat(updated.afterValue()).isEqualTo("3분");

		taskResultService.deleteResult(task.id(), withBefore.id(), owner.getId());
		assertThat(taskResultService.getResults(task.id(), owner.getId())).hasSize(1);
	}

	@Test
	@DisplayName("남의 업무의 성과 항목은 조회·등록할 수 없다")
	void otherMembersTask_throwsAccessDenied() {
		TaskResponse othersTask = taskService.createTask(other.getId(),
				new TaskRequest("남의 업무", null, TaskPriority.MEDIUM, null, otherProject.getId(), List.of(), List.of()));

		assertThatThrownBy(() -> taskResultService.getResults(othersTask.id(), owner.getId()))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.TASK_ACCESS_DENIED);

		assertThatThrownBy(() -> taskResultService.createResult(othersTask.id(), owner.getId(),
				new TaskResultRequest("지표", null, "값")))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.TASK_ACCESS_DENIED);
	}

	@Test
	@DisplayName("다른 업무에 달린 성과 항목을 내 업무 주소로 수정하려 하면 TASK_RESULT_NOT_FOUND가 발생한다")
	void updateResultOfDifferentTask_throwsNotFound() {
		TaskResponse taskA = createTask("업무 A");
		TaskResponse taskB = createTask("업무 B");
		TaskResultResponse resultOfA = taskResultService.createResult(taskA.id(), owner.getId(),
				new TaskResultRequest("지표", null, "값"));

		// 내 업무이긴 하지만 이 성과 항목은 A에 붙어 있다 — B 주소로 접근하면 안 된다
		assertThatThrownBy(() -> taskResultService.updateResult(taskB.id(), resultOfA.id(), owner.getId(),
				new TaskResultRequest("지표", null, "바꿈")))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.TASK_RESULT_NOT_FOUND);
	}

}
