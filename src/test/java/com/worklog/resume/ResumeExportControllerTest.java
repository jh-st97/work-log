package com.worklog.resume;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.worklog.support.ControllerTestSupport;

class ResumeExportControllerTest extends ControllerTestSupport {

	@Test
	@DisplayName("토큰 없이 내보내기를 부르면 401")
	void export_withoutToken_returnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/exports/resume").param("taskIds", "1"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("내 업무를 내보내면 200과 마크다운이 나온다 (여러 개는 taskIds를 반복해서 보낸다)")
	void export_ownTasks_returnsMarkdown() throws Exception {
		String token = signupAndLogin("a@test.com");
		long projectId = createProject(token, "my project");
		long first = createTask(token, projectId, "first task");
		long second = createTask(token, projectId, "second task");

		mockMvc.perform(get("/api/exports/resume").header("Authorization", bearer(token))
				.param("taskIds", String.valueOf(first), String.valueOf(second)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.markdown").value(Matchers.containsString("## first task")))
				.andExpect(jsonPath("$.markdown").value(Matchers.containsString("## second task")));
	}

	@Test
	@DisplayName("남의 업무를 내보내려 하면 403 TASK_ACCESS_DENIED")
	void export_otherMembersTask_returnsForbidden() throws Exception {
		String tokenA = signupAndLogin("a@test.com");
		String tokenB = signupAndLogin("b@test.com");
		long taskOfA = createTask(tokenA, createProject(tokenA, "a project"), "a task");

		mockMvc.perform(get("/api/exports/resume").header("Authorization", bearer(tokenB))
				.param("taskIds", String.valueOf(taskOfA)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("TASK_ACCESS_DENIED"));
	}

	@Test
	@DisplayName("taskIds를 안 보내면 400 INVALID_INPUT")
	void export_withoutTaskIds_returnsBadRequest() throws Exception {
		String token = signupAndLogin("a@test.com");

		mockMvc.perform(get("/api/exports/resume").header("Authorization", bearer(token)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_INPUT"));
	}

}
