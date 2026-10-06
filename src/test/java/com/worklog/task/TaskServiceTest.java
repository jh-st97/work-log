package com.worklog.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;

import com.worklog.common.exception.BusinessException;
import com.worklog.common.exception.ErrorCode;
import com.worklog.member.Member;
import com.worklog.member.MemberRepository;
import com.worklog.project.Project;
import com.worklog.project.ProjectRepository;
import com.worklog.tag.Tag;
import com.worklog.tag.TagRepository;
import com.worklog.task.dto.TaskRequest;
import com.worklog.task.dto.TaskResponse;
import com.worklog.task.dto.TaskStatusRequest;
import com.worklog.worksystem.WorkSystem;
import com.worklog.worksystem.WorkSystemRepository;

// 서비스를 진짜 DB(work_log_test)에 연결해서 테스트한다.
// 일부러 @Transactional을 안 붙였다 — 테스트 전체를 하나의 트랜잭션으로 감싸면 마지막에 롤백되면서
// "DELETE/INSERT 순서" 같은 DB 제약 문제가 실제로 실행되지 않아서, 우리가 겪은 버그를 못 잡는다.
@SpringBootTest
class TaskServiceTest {

	@Autowired
	private TaskService taskService;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private TagRepository tagRepository;
	@Autowired
	private WorkSystemRepository workSystemRepository;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	private Member owner;
	private Member other;
	private Project project;
	private Tag tag;
	private WorkSystem system;

	private final Pageable firstPage = PageRequest.of(0, 20);

	@BeforeEach
	void setUp() {
		// 테스트마다 데이터를 전부 비우고 시작한다 (member를 비우면 CASCADE로 하위 테이블도 같이 비워진다)
		jdbcTemplate.execute("TRUNCATE TABLE member RESTART IDENTITY CASCADE");

		owner = memberRepository.save(new Member("owner@test.com", "pw", "owner"));
		other = memberRepository.save(new Member("other@test.com", "pw", "other"));
		project = projectRepository.save(new Project(owner, "테스트 프로젝트", null, null, null));
		tag = tagRepository.save(new Tag(owner, "Spring"));
		system = workSystemRepository.save(new WorkSystem(owner, "회원 시스템", null));
	}

	private TaskResponse createTask(String title, TaskPriority priority, List<Long> tagIds, List<Long> systemIds) {
		return taskService.createTask(owner.getId(),
				new TaskRequest(title, null, priority, null, project.getId(), tagIds, systemIds));
	}

	@Test
	@DisplayName("업무를 수정할 때 기존과 같은 태그·시스템을 그대로 두어도 중복 키 오류가 나지 않는다")
	void updateTask_keepingSameTagAndSystem_doesNotViolateUniqueConstraint() {
		TaskResponse created = createTask("원래 제목", TaskPriority.MEDIUM, List.of(tag.getId()), List.of(system.getId()));

		TaskRequest sameLinks = new TaskRequest("바뀐 제목", null, TaskPriority.HIGH, null, project.getId(),
				List.of(tag.getId()), List.of(system.getId()));

		// flush() 없이 "삭제 후 재생성"을 하면 INSERT가 DELETE보다 먼저 실행돼서 task_tag의 (task_id, tag_id) 유니크 제약에 걸린다
		assertThatCode(() -> taskService.updateTask(created.id(), owner.getId(), sameLinks)).doesNotThrowAnyException();

		TaskResponse updated = taskService.getTask(created.id(), owner.getId());
		assertThat(updated.title()).isEqualTo("바뀐 제목");
		assertThat(updated.tags()).hasSize(1);
		assertThat(updated.systems()).hasSize(1);
	}

	@Test
	@DisplayName("남의 업무를 수정하거나 조회하려 하면 TASK_ACCESS_DENIED가 발생한다")
	void accessOtherMembersTask_throwsAccessDenied() {
		TaskResponse created = createTask("내 업무", TaskPriority.MEDIUM, List.of(), List.of());

		TaskRequest request = new TaskRequest("가로채기", null, TaskPriority.LOW, null, project.getId(), List.of(),
				List.of());

		assertThatThrownBy(() -> taskService.updateTask(created.id(), other.getId(), request))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.TASK_ACCESS_DENIED);

		assertThatThrownBy(() -> taskService.getTask(created.id(), other.getId()))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.TASK_ACCESS_DENIED);
	}

	@Test
	@DisplayName("업무 목록은 필터 조건대로 걸러지고, 남의 업무와 보관한 업무는 나오지 않는다")
	void getTasks_appliesFilters() {
		TaskResponse deploy = createTask("배포 자동화", TaskPriority.HIGH, List.of(tag.getId()), List.of(system.getId()));
		TaskResponse bug = createTask("버그 수정", TaskPriority.LOW, List.of(), List.of());
		TaskResponse archived = createTask("보관할 업무", TaskPriority.HIGH, List.of(), List.of());

		taskService.changeStatus(deploy.id(), owner.getId(), new TaskStatusRequest(TaskStatus.DONE));
		taskService.archiveTask(archived.id(), owner.getId());

		// 남의 업무: 내 목록에는 절대 나오면 안 된다
		taskService.createTask(other.getId(), new TaskRequest("남의 업무", null, TaskPriority.HIGH, null,
				projectRepository.save(new Project(other, "남의 프로젝트", null, null, null)).getId(), List.of(), List.of()));

		// 필터 없음 → 내 업무 중 보관 안 된 2개
		assertThat(titles(null, null, null, null, null)).containsExactlyInAnyOrder("배포 자동화", "버그 수정");

		// 우선순위
		assertThat(titles(null, TaskPriority.HIGH, null, null, null)).containsExactly("배포 자동화");

		// 상태
		assertThat(titles(TaskStatus.DONE, null, null, null, null)).containsExactly("배포 자동화");
		assertThat(titles(TaskStatus.TODO, null, null, null, null)).containsExactly("버그 수정");

		// 태그 / 시스템 (서브쿼리 조건)
		assertThat(titles(null, null, tag.getId(), null, null)).containsExactly("배포 자동화");
		assertThat(titles(null, null, null, system.getId(), null)).containsExactly("배포 자동화");

		// 키워드 (제목, 대소문자 무시)
		assertThat(titles(null, null, null, null, "버그")).containsExactly("버그 수정");

		// 조건 조합(AND): 우선순위 LOW이면서 태그가 달린 업무는 없다
		assertThat(titles(null, TaskPriority.LOW, tag.getId(), null, null)).isEmpty();

		// 없는 태그
		assertThat(titles(null, null, 9999L, null, null)).isEmpty();

		// 태그·시스템도 N+1 개선 이후 정확히 붙어서 나온다
		TaskResponse found = taskService.getTasks(owner.getId(), null, TaskPriority.HIGH, null, null, null, null, null,
				null, firstPage).getContent().get(0);
		assertThat(found.tags()).extracting(t -> t.name()).containsExactly("Spring");
		assertThat(found.systems()).extracting(s -> s.name()).containsExactly("회원 시스템");

		assertThat(bug.id()).isNotNull();
	}

	private List<String> titles(TaskStatus status, TaskPriority priority, Long tagId, Long systemId, String keyword) {
		return taskService.getTasks(owner.getId(), status, priority, null, systemId, tagId, null, null, keyword,
				firstPage).getContent().stream().map(TaskResponse::title).toList();
	}

}
