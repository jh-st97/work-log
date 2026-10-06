package com.worklog.worksystem;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.worklog.support.ServiceTestSupport;
import com.worklog.task.TaskPriority;
import com.worklog.task.dto.TaskRequest;
import com.worklog.task.dto.TaskResponse;
import com.worklog.worksystem.dto.WorkSystemRequest;
import com.worklog.worksystem.dto.WorkSystemResponse;

class WorkSystemServiceTest extends ServiceTestSupport {

	@Autowired
	private WorkSystemService workSystemService;
	@Autowired
	private WorkSystemRepository workSystemRepository;

	@Test
	@DisplayName("업무에 붙어 있는 업무 시스템을 삭제하면 연결만 끊기고, 업무는 그대로 남는다")
	void deleteWorkSystem_attachedToTask_unlinksAndKeepsTask() {
		WorkSystemResponse system = workSystemService.createWorkSystem(owner.getId(),
				new WorkSystemRequest("회원 시스템", null));
		WorkSystemResponse remaining = workSystemService.createWorkSystem(owner.getId(),
				new WorkSystemRequest("결제 시스템", null));
		TaskResponse task = taskService.createTask(owner.getId(), new TaskRequest("로그인 개선", null,
				TaskPriority.MEDIUM, null, project.getId(), List.of(), List.of(system.id(), remaining.id())));

		// FK 때문에 예전에는 여기서 DB 오류(500)가 났다
		workSystemService.deleteWorkSystem(system.id(), owner.getId());

		assertThat(workSystemRepository.findById(system.id())).isEmpty();
		TaskResponse after = taskService.getTask(task.id(), owner.getId());
		assertThat(after.title()).isEqualTo("로그인 개선");
		assertThat(after.systems()).extracting(WorkSystemResponse::name).containsExactly("결제 시스템");
	}

}
