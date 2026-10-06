package com.worklog.support;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.worklog.member.Member;
import com.worklog.member.MemberRepository;
import com.worklog.project.Project;
import com.worklog.project.ProjectRepository;
import com.worklog.task.TaskPriority;
import com.worklog.task.TaskService;
import com.worklog.task.dto.TaskRequest;
import com.worklog.task.dto.TaskResponse;

// 서비스 테스트들이 같이 쓰는 준비 코드. 상속해서 쓴다.
// 테스트 클래스에 @Transactional을 안 붙이는 이유는 TaskServiceTest의 설명 참고
// (붙이면 롤백되면서 DB 제약 문제가 실제로 실행되지 않는다).
@SpringBootTest
public abstract class ServiceTestSupport {

	@Autowired
	protected MemberRepository memberRepository;
	@Autowired
	protected ProjectRepository projectRepository;
	@Autowired
	protected TaskService taskService;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	protected Member owner;
	protected Member other;
	protected Project project;
	protected Project otherProject;

	@BeforeEach
	void resetData() {
		// member를 비우면 CASCADE로 하위 테이블도 전부 비워진다
		jdbcTemplate.execute("TRUNCATE TABLE member RESTART IDENTITY CASCADE");

		owner = memberRepository.save(new Member("owner@test.com", "pw", "owner"));
		other = memberRepository.save(new Member("other@test.com", "pw", "other"));
		project = projectRepository.save(new Project(owner, "내 프로젝트", null, null, null));
		otherProject = projectRepository.save(new Project(other, "남의 프로젝트", null, null, null));
	}

	// 내(owner) 업무 하나를 만든다
	protected TaskResponse createTask(String title) {
		return taskService.createTask(owner.getId(),
				new TaskRequest(title, null, TaskPriority.MEDIUM, null, project.getId(), List.of(), List.of()));
	}

}
