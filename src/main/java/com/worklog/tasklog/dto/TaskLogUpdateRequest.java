package com.worklog.tasklog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

// 진행 메모 수정용. 업무는 등록 후 바꿀 수 없어서 taskId가 없다.
public record TaskLogUpdateRequest(

		@NotBlank(message = "내용을 입력해 주세요.")
		String content,

		@Min(value = 0, message = "소요 시간은 0 이상이어야 합니다.")
		Integer spentMinutes

) {
}
