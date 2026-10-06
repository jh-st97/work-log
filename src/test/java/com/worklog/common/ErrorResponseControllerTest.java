package com.worklog.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.worklog.support.ControllerTestSupport;

// 잘못된 요청이 들어왔을 때 응답 코드가 상황에 맞게 나오는지(404/405/400) 확인한다.
// 예전에는 이런 경우가 전부 500(또는 엉뚱한 401)으로 뭉뚱그려져 원인 찾기가 어려웠다.
class ErrorResponseControllerTest extends ControllerTestSupport {

	@Test
	@DisplayName("로그인한 상태에서 없는 주소를 부르면 404 RESOURCE_NOT_FOUND")
	void unknownPath_returnsNotFound() throws Exception {
		String token = signupAndLogin("a@test.com");

		mockMvc.perform(get("/api/totally-made-up").header("Authorization", bearer(token)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
	}

	@Test
	@DisplayName("로그인 안 한 채로 없는 주소를 부르면 주소가 있는지 알려주지 않고 401")
	void unknownPath_withoutToken_returnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/totally-made-up"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("GET만 있는 주소에 POST를 보내면 405 METHOD_NOT_ALLOWED")
	void wrongMethod_returnsMethodNotAllowed() throws Exception {
		String token = signupAndLogin("a@test.com");

		mockMvc.perform(post("/api/members/me").header("Authorization", bearer(token)))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
	}

	@Test
	@DisplayName("깨진 JSON을 보내면 400 INVALID_INPUT")
	void malformedJson_returnsBadRequest() throws Exception {
		String token = signupAndLogin("a@test.com");

		mockMvc.perform(post("/api/projects").header("Authorization", bearer(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\": "))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_INPUT"));
	}

	@Test
	@DisplayName("번호 자리에 글자가 오거나 날짜 형식이 틀리면 400 INVALID_INPUT")
	void wrongTypeParameter_returnsBadRequest() throws Exception {
		String token = signupAndLogin("a@test.com");

		mockMvc.perform(get("/api/tasks/abc").header("Authorization", bearer(token)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_INPUT"));

		mockMvc.perform(get("/api/daily-logs/not-a-date").header("Authorization", bearer(token)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_INPUT"));
	}

}
