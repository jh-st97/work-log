package com.worklog.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.worklog.support.ControllerTestSupport;

// 프로젝트·태그·업무 시스템 목록이 page/size를 받아 페이지 단위로 응답하는지 확인한다.
class ListPagingControllerTest extends ControllerTestSupport {

	private void createTag(String token, String name) throws Exception {
		mockMvc.perform(post("/api/tags").header("Authorization", bearer(token))
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"" + name + "\"}"))
				.andExpect(status().isCreated());
	}

	private void createSystem(String token, String name) throws Exception {
		mockMvc.perform(post("/api/systems").header("Authorization", bearer(token))
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"" + name + "\"}"))
				.andExpect(status().isCreated());
	}

	@Test
	@DisplayName("태그 목록은 size만큼만 나오고, 전체 개수와 페이지 수를 알려준다 (먼저 만든 순서)")
	void tags_arePaged() throws Exception {
		String token = signupAndLogin("a@test.com");
		createTag(token, "first");
		createTag(token, "second");
		createTag(token, "third");

		mockMvc.perform(get("/api/tags").header("Authorization", bearer(token)).param("size", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(2))
				.andExpect(jsonPath("$.content[0].name").value("first"))
				.andExpect(jsonPath("$.totalElements").value(3))
				.andExpect(jsonPath("$.totalPages").value(2));

		mockMvc.perform(get("/api/tags").header("Authorization", bearer(token)).param("size", "2").param("page", "1"))
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].name").value("third"));
	}

	@Test
	@DisplayName("업무 시스템 목록도 페이지 단위로 나오고, 남의 것은 섞이지 않는다")
	void systems_arePagedAndScopedToMember() throws Exception {
		String tokenA = signupAndLogin("a@test.com");
		String tokenB = signupAndLogin("b@test.com");
		createSystem(tokenA, "a-one");
		createSystem(tokenA, "a-two");
		createSystem(tokenB, "b-one");

		mockMvc.perform(get("/api/systems").header("Authorization", bearer(tokenA)).param("size", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.totalElements").value(2))
				.andExpect(jsonPath("$.totalPages").value(2));
	}

	@Test
	@DisplayName("프로젝트 목록도 페이지 단위로 나오고, 보관한 프로젝트는 빠진다")
	void projects_arePagedAndExcludeArchived() throws Exception {
		String token = signupAndLogin("a@test.com");
		createProject(token, "p1");
		long archived = createProject(token, "p2");
		createProject(token, "p3");

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
				.delete("/api/projects/" + archived).header("Authorization", bearer(token)))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/projects").header("Authorization", bearer(token)).param("size", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].name").value("p1"))
				.andExpect(jsonPath("$.totalElements").value(2));
	}

	@Test
	@DisplayName("size를 안 보내면 기본 20개씩 나온다")
	void defaultSizeIs20() throws Exception {
		String token = signupAndLogin("a@test.com");
		createTag(token, "only");

		mockMvc.perform(get("/api/tags").header("Authorization", bearer(token)))
				.andExpect(jsonPath("$.size").value(20));
	}

}
