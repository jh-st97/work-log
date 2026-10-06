package com.worklog.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.worklog.common.exception.BusinessException;
import com.worklog.common.exception.ErrorCode;
import com.worklog.project.ProjectService;
import com.worklog.project.dto.ProjectResponse;
import com.worklog.support.ServiceTestSupport;
import com.worklog.task.dto.TaskRequest;
import com.worklog.task.dto.TaskResponse;

// 업무·프로젝트의 보관(숨기기)과 복구(되살리기) 규칙을 확인한다.
class ArchiveRestoreServiceTest extends ServiceTestSupport {

	@Autowired
	private ProjectService projectService;

	private final Pageable firstPage = PageRequest.of(0, 20);

	private List<String> activeTitles() {
		return taskService.getTasks(owner.getId(), null, null, null, null, null, null, null, null, false, firstPage)
				.getContent().stream().map(TaskResponse::title).toList();
	}

	private List<String> archivedTitles() {
		return taskService.getTasks(owner.getId(), null, null, null, null, null, null, null, null, true, firstPage)
				.getContent().stream().map(TaskResponse::title).toList();
	}

	@Test
	@DisplayName("업무를 보관하면 보통 목록에서 빠지고 보관함에 나오며, 복구하면 다시 보통 목록으로 돌아온다")
	void archiveAndRestoreTask() {
		TaskResponse task = createTask("배포 자동화");

		taskService.archiveTask(task.id(), owner.getId());
		assertThat(activeTitles()).isEmpty();
		assertThat(archivedTitles()).containsExactly("배포 자동화");

		taskService.restoreTask(task.id(), owner.getId());
		assertThat(activeTitles()).containsExactly("배포 자동화");
		assertThat(archivedTitles()).isEmpty();
	}

	@Test
	@DisplayName("프로젝트를 보관하면 그 프로젝트의 업무도 보통 목록에서 함께 숨겨진다")
	void archivingProject_hidesItsTasks() {
		createTask("내 업무");

		projectService.archiveProject(project.getId(), owner.getId());

		assertThat(activeTitles()).isEmpty();
		// 업무 자체를 보관한 게 아니라서 업무 보관함에도 나오지 않는다
		assertThat(archivedTitles()).isEmpty();
	}

	@Test
	@DisplayName("프로젝트를 복구하면 하위 업무가 다시 보인다")
	void restoringProject_showsTasksAgain() {
		createTask("내 업무");
		projectService.archiveProject(project.getId(), owner.getId());

		ProjectResponse restored = projectService.restoreProject(project.getId(), owner.getId());

		assertThat(restored.id()).isEqualTo(project.getId());
		assertThat(activeTitles()).containsExactly("내 업무");
	}

	@Test
	@DisplayName("프로젝트 복구는 직접 보관해 둔 업무까지 되살리지 않는다")
	void restoringProject_keepsIndividuallyArchivedTasksArchived() {
		TaskResponse archivedTask = createTask("직접 보관한 업무");
		createTask("그냥 업무");
		taskService.archiveTask(archivedTask.id(), owner.getId());
		projectService.archiveProject(project.getId(), owner.getId());

		projectService.restoreProject(project.getId(), owner.getId());

		assertThat(activeTitles()).containsExactly("그냥 업무");
		assertThat(archivedTitles()).containsExactly("직접 보관한 업무");
	}

	@Test
	@DisplayName("프로젝트가 보관 중이면 그 업무는 복구할 수 없다 (PROJECT_ARCHIVED)")
	void restoreTask_whenProjectArchived_throwsConflict() {
		TaskResponse task = createTask("내 업무");
		taskService.archiveTask(task.id(), owner.getId());
		projectService.archiveProject(project.getId(), owner.getId());

		assertThatThrownBy(() -> taskService.restoreTask(task.id(), owner.getId()))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.PROJECT_ARCHIVED);
	}

	@Test
	@DisplayName("보관 중인 프로젝트에는 업무를 새로 만들 수 없다 (PROJECT_ARCHIVED)")
	void createTask_inArchivedProject_throwsConflict() {
		projectService.archiveProject(project.getId(), owner.getId());

		assertThatThrownBy(() -> taskService.createTask(owner.getId(),
				new TaskRequest("새 업무", null, TaskPriority.MEDIUM, null, project.getId(), List.of(), List.of())))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.PROJECT_ARCHIVED);
	}

	@Test
	@DisplayName("남의 업무나 프로젝트는 복구할 수 없다")
	void restore_otherMembersItems_throwsAccessDenied() {
		TaskResponse task = createTask("내 업무");
		taskService.archiveTask(task.id(), owner.getId());
		projectService.archiveProject(project.getId(), owner.getId());

		assertThatThrownBy(() -> taskService.restoreTask(task.id(), other.getId()))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.TASK_ACCESS_DENIED);

		assertThatThrownBy(() -> projectService.restoreProject(project.getId(), other.getId()))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.PROJECT_ACCESS_DENIED);
	}

}
