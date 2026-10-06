package com.worklog.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.worklog.common.exception.BusinessException;
import com.worklog.common.exception.ErrorCode;
import com.worklog.support.ServiceTestSupport;
import com.worklog.tag.dto.TagRequest;
import com.worklog.tag.dto.TagResponse;
import com.worklog.task.TaskPriority;
import com.worklog.task.dto.TaskRequest;
import com.worklog.task.dto.TaskResponse;

// ServiceTestSupport를 상속하면 아래가 이미 준비되어 있다.
//   - owner : 내 회원 (owner.getId()로 번호를 꺼낸다)
//   - other : 다른 회원
//   - 테스트마다 DB가 깨끗하게 비워진 상태로 시작한다
class TagServiceTest extends ServiceTestSupport {

	@Autowired
	private TagService tagService;

	@Test
	@DisplayName("같은 이름의 태그를 두 번 등록하면 TAG_DUPLICATED가 발생한다")
	void createTag_duplicated_throws() {
		// 1. 준비: "Spring"이라는 태그를 하나 등록한다
		//    힌트: tagService.createTag(owner.getId(), new TagRequest("Spring"));
		TagResponse tag = tagService.createTag(owner.getId(), new TagRequest("Spring"));
		

		// 2. 실행 + 확인: 같은 이름으로 또 등록하면 BusinessException이 나는지 확인한다
		//    힌트: TaskResultServiceTest의 assertThatThrownBy(...) 부분을 그대로 따라 쓰면 된다
		//    확인할 에러 코드: ErrorCode.TAG_DUPLICATED
		assertThatThrownBy(() -> tagService.createTag(owner.getId(), new TagRequest("Spring")))
		.isInstanceOf(BusinessException.class)
		.extracting(e -> ((BusinessException) e).getErrorCode())
		.isEqualTo(ErrorCode.TAG_DUPLICATED);
	}

	@Test
	@DisplayName("업무에 붙어 있는 태그를 삭제하면 연결만 끊기고, 업무는 그대로 남는다")
	void deleteTag_attachedToTask_unlinksAndKeepsTask() {
		// 1. 준비: 태그 2개를 만들고, 둘 다 붙인 업무를 하나 만든다
		//    힌트: WorkSystemServiceTest의 같은 이름 테스트를 보고 태그 버전으로 바꿔 쓴다
		//    - tagService.createTag(owner.getId(), new TagRequest("Spring")) 의 결과(TagResponse)에서 .id()를 꺼낼 수 있다
		//    - 업무는 taskService.createTask(owner.getId(), new TaskRequest(제목, null, TaskPriority.MEDIUM, null,
		//      project.getId(), List.of(태그id들...), List.of())) 로 만든다 (taskService, project는 부모 클래스에 있다)

		// 태그 2개 만들기 (각각 결과를 변수에 담아 두면 .id()와 .name()을 꺼낼 수 있어요)
		TagResponse spring = tagService.createTag(owner.getId(), new TagRequest("Spring"));
		TagResponse jpa = tagService.createTag(owner.getId(),new TagRequest("JPA"));
		
		// 둘 다 붙인 업무 만들기
		TaskResponse task = taskService.createTask(owner.getId(),
		        new TaskRequest("로그인 개선", null, TaskPriority.MEDIUM, null,
		                project.getId(), List.of(spring.id(), jpa.id()), List.of()));
		
		// 2. 실행: 태그 하나를 삭제한다
		//    tagService.deleteTag(태그id, owner.getId());
		tagService.deleteTag(spring.id(), owner.getId());

		// 3. 확인: 업무는 그대로 있고(taskService.getTask(...)), 남은 태그 하나만 붙어 있는지 본다
		//    assertThat(after.tags()).extracting(TagResponse::name).containsExactly("남은 태그 이름");
		TaskResponse after = taskService.getTask(task.id(), owner.getId());
		assertThat(after.tags()).extracting(TagResponse::name).containsExactly("JPA");

		// 아직 안 썼다는 표시. 다 채웠으면 이 줄은 지운다 (지우기 전에는 빨간색으로 실패해야 정상)
		
	}

}
