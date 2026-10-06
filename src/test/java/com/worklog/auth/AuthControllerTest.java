package com.worklog.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.worklog.support.ControllerTestSupport;

class AuthControllerTest extends ControllerTestSupport {

	private String credentials(String email, String password) {
		return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"nickname\":\"tester\"}";
	}

	@Test
	@DisplayName("회원가입하면 201이고, 응답에 비밀번호는 담기지 않는다")
	void signup_returnsCreatedWithoutPassword() throws Exception {
		mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
				.content(credentials("new@test.com", PASSWORD)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("new@test.com"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	@DisplayName("이미 가입된 이메일로 가입하면 409 EMAIL_DUPLICATED")
	void signup_duplicatedEmail_returnsConflict() throws Exception {
		signupAndLogin("dup@test.com");

		mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
				.content(credentials("dup@test.com", PASSWORD)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("EMAIL_DUPLICATED"));
	}

	@Test
	@DisplayName("비밀번호가 8자 미만이면 400 INVALID_INPUT")
	void signup_shortPassword_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
				.content(credentials("short@test.com", "1234")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_INPUT"));
	}

	@Test
	@DisplayName("로그인하면 200과 토큰이 나온다")
	void login_success_returnsToken() throws Exception {
		signupAndLogin("login@test.com");

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"login@test.com\",\"password\":\"" + PASSWORD + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty());
	}

	@Test
	@DisplayName("비밀번호가 틀리든 없는 이메일이든 똑같이 401 LOGIN_FAILED (어느 쪽이 틀렸는지 알려주지 않는다)")
	void login_failure_returnsSameUnauthorized() throws Exception {
		signupAndLogin("login@test.com");

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"login@test.com\",\"password\":\"wrong-password-1\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("LOGIN_FAILED"));

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"nobody@test.com\",\"password\":\"" + PASSWORD + "\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("LOGIN_FAILED"));
	}

	@Test
	@DisplayName("유효한 토큰으로 내 정보를 조회하면 200")
	void me_withValidToken_returnsOk() throws Exception {
		String token = signupAndLogin("me@test.com");

		mockMvc.perform(get("/api/members/me").header("Authorization", bearer(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("me@test.com"));
	}

	@Test
	@DisplayName("토큰이 없거나 가짜 토큰이면 401 UNAUTHORIZED")
	void me_withoutOrFakeToken_returnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/members/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

		mockMvc.perform(get("/api/members/me").header("Authorization", "Bearer fake.token.value"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

}
