package com.worklog.task;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.worklog.support.ControllerTestSupport;

// 서비스 테스트가 "규칙이 맞나"를 본다면, 이 테스트는 "HTTP로 요청했을 때 응답 코드와 보안이 맞나"를 본다.
class TaskControllerTest extends ControllerTestSupport {

	@Test
	@DisplayName("토큰 없이 보호된 API를 부르면 401")
	void protectedApi_withoutToken_returnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/tasks")).andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/projects")).andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/daily-logs/2026-09-23")).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Swagger 설명서는 토큰 없이 열리지만, 실제 API는 여전히 막혀 있다")
	void swaggerDocs_openButApiStillProtected() throws Exception {
		mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
		mockMvc.perform(get("/api/tasks")).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("업무를 만들면 201이고, 내 목록에 나온다")
	void createTask_thenAppearsInMyList() throws Exception {
		String token = signupAndLogin("a@test.com");
		long projectId = createProject(token, "my project");
		createTask(token, projectId, "first task");

		mockMvc.perform(get("/api/tasks").header("Authorization", bearer(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.content[0].title").value("first task"));
	}

	@Test
	@DisplayName("남의 업무는 내 목록에 안 나오고, 상세 조회와 수정은 403 TASK_ACCESS_DENIED")
	void otherMembersTask_isHiddenAndForbidden() throws Exception {
		String tokenA = signupAndLogin("a@test.com");
		String tokenB = signupAndLogin("b@test.com");
		long projectId = createProject(tokenA, "a project");
		long taskId = createTask(tokenA, projectId, "a task");

		// B의 목록은 비어 있다
		mockMvc.perform(get("/api/tasks").header("Authorization", bearer(tokenB)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(0));

		// B가 A의 업무를 직접 조회·수정하려 하면 403
		mockMvc.perform(get("/api/tasks/" + taskId).header("Authorization", bearer(tokenB)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("TASK_ACCESS_DENIED"));

		mockMvc.perform(patch("/api/tasks/" + taskId).header("Authorization", bearer(tokenB))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"hijack\",\"projectId\":" + projectId + "}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("TASK_ACCESS_DENIED"));
	}

	@Test
	@DisplayName("없는 업무를 조회하면 404 TASK_NOT_FOUND")
	void getTask_whenNotExists_returnsNotFound() throws Exception {
		String token = signupAndLogin("a@test.com");

		mockMvc.perform(get("/api/tasks/9999").header("Authorization", bearer(token)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"));
	}

	@Test
	@DisplayName("제목이 비었거나 프로젝트가 없으면 400 INVALID_INPUT")
	void createTask_withInvalidInput_returnsBadRequest() throws Exception {
		String token = signupAndLogin("a@test.com");
		long projectId = createProject(token, "my project");

		mockMvc.perform(post("/api/tasks").header("Authorization", bearer(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"\",\"projectId\":" + projectId + "}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_INPUT"));

		mockMvc.perform(post("/api/tasks").header("Authorization", bearer(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"no project\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_INPUT"));
	}

	@Test
	@DisplayName("기간 조회에 from/to를 빠뜨리면 401이 아니라 400 INVALID_INPUT")
	void dailyLogs_missingRequiredParams_returnsBadRequest() throws Exception {
		String token = signupAndLogin("a@test.com");

		mockMvc.perform(get("/api/daily-logs").header("Authorization", bearer(token)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_INPUT"));
	}

	@Test
	@DisplayName("남의 프로젝트에 업무를 등록하려 하면 403 PROJECT_ACCESS_DENIED")
	void createTask_inOtherMembersProject_returnsForbidden() throws Exception {
		String tokenA = signupAndLogin("a@test.com");
		String tokenB = signupAndLogin("b@test.com");
		long projectOfA = createProject(tokenA, "a project");

		mockMvc.perform(post("/api/tasks").header("Authorization", bearer(tokenB))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"sneak\",\"projectId\":" + projectOfA + "}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("PROJECT_ACCESS_DENIED"));
	}

}
