package com.worklog.task;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.worklog.support.ControllerTestSupport;

class ArchiveRestoreControllerTest extends ControllerTestSupport {

	@Test
	@DisplayName("업무를 보관하면 archived=true 목록에 나오고, 복구하면 보통 목록으로 돌아온다")
	void archiveThenRestoreTask() throws Exception {
		String token = signupAndLogin("a@test.com");
		long taskId = createTask(token, createProject(token, "p"), "t1");

		mockMvc.perform(delete("/api/tasks/" + taskId).header("Authorization", bearer(token)))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/tasks").header("Authorization", bearer(token)))
				.andExpect(jsonPath("$.totalElements").value(0));
		mockMvc.perform(get("/api/tasks").param("archived", "true").header("Authorization", bearer(token)))
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.content[0].title").value("t1"));

		mockMvc.perform(post("/api/tasks/" + taskId + "/restore").header("Authorization", bearer(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("t1"));

		mockMvc.perform(get("/api/tasks").header("Authorization", bearer(token)))
				.andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	@DisplayName("프로젝트를 보관하면 업무가 숨겨지고, 보관 중인 프로젝트의 업무 복구는 409 PROJECT_ARCHIVED")
	void archivedProject_hidesTasksAndBlocksTaskRestore() throws Exception {
		String token = signupAndLogin("a@test.com");
		long projectId = createProject(token, "p");
		long taskId = createTask(token, projectId, "t1");

		mockMvc.perform(delete("/api/tasks/" + taskId).header("Authorization", bearer(token)))
				.andExpect(status().isNoContent());
		mockMvc.perform(delete("/api/projects/" + projectId).header("Authorization", bearer(token)))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/tasks/" + taskId + "/restore").header("Authorization", bearer(token)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("PROJECT_ARCHIVED"));

		// 프로젝트부터 복구하면, 그다음 업무 복구가 된다
		mockMvc.perform(get("/api/projects").param("archived", "true").header("Authorization", bearer(token)))
				.andExpect(jsonPath("$.content[0].name").value("p"));
		mockMvc.perform(post("/api/projects/" + projectId + "/restore").header("Authorization", bearer(token)))
				.andExpect(status().isOk());
		mockMvc.perform(post("/api/tasks/" + taskId + "/restore").header("Authorization", bearer(token)))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("남의 업무·프로젝트를 복구하려 하면 403")
	void restoreOtherMembersItems_returnsForbidden() throws Exception {
		String tokenA = signupAndLogin("a@test.com");
		String tokenB = signupAndLogin("b@test.com");
		long projectId = createProject(tokenA, "p");
		long taskId = createTask(tokenA, projectId, "t1");

		mockMvc.perform(post("/api/tasks/" + taskId + "/restore").header("Authorization", bearer(tokenB)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("TASK_ACCESS_DENIED"));
		mockMvc.perform(post("/api/projects/" + projectId + "/restore").header("Authorization", bearer(tokenB)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("PROJECT_ACCESS_DENIED"));
	}

	@Test
	@DisplayName("토큰 없이 복구를 부르면 401")
	void restore_withoutToken_returnsUnauthorized() throws Exception {
		mockMvc.perform(post("/api/tasks/1/restore")).andExpect(status().isUnauthorized());
		mockMvc.perform(post("/api/projects/1/restore")).andExpect(status().isUnauthorized());
	}

}
