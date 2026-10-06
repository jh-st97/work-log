package com.worklog.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.worklog.common.exception.BusinessException;
import com.worklog.common.exception.ErrorCode;
import com.worklog.support.ServiceTestSupport;
import com.worklog.tag.dto.TagRequest;
import com.worklog.tag.dto.TagResponse;
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

}
