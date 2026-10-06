package com.worklog.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

// 컨트롤러 통합 테스트들이 같이 쓰는 준비 코드. 상속해서 쓴다.
// MockMvc는 서버를 실제로 띄우지 않고, "HTTP 요청이 들어온 것처럼" 보안 필터(JWT)·컨트롤러·서비스·DB를
// 전부 거쳐 가게 해준다. 그래서 응답 코드(200/400/401/403)와 JSON 응답까지 확인할 수 있다.
@SpringBootTest
@AutoConfigureMockMvc
public abstract class ControllerTestSupport {

	protected static final String PASSWORD = "password-1234";

	@Autowired
	protected MockMvc mockMvc;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void resetData() {
		jdbcTemplate.execute("TRUNCATE TABLE member RESTART IDENTITY CASCADE");
	}

	// 가입하고 로그인해서 토큰을 돌려준다
	protected String signupAndLogin(String email) throws Exception {
		mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\",\"nickname\":\"tester\"}"))
				.andExpect(status().isCreated());

		String body = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return JsonPath.read(body, "$.accessToken");
	}

	protected static String bearer(String token) {
		return "Bearer " + token;
	}

	protected long createProject(String token, String name) throws Exception {
		String body = mockMvc.perform(post("/api/projects").header("Authorization", bearer(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"" + name + "\"}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(body, "$.id")).longValue();
	}

	protected long createTask(String token, long projectId, String title) throws Exception {
		String body = mockMvc.perform(post("/api/tasks").header("Authorization", bearer(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"" + title + "\",\"projectId\":" + projectId + ",\"tagIds\":[],\"systemIds\":[]}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(body, "$.id")).longValue();
	}

}
