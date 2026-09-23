package com.worklog.tasklog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 진행 메모 등록용. 날짜는 URL 경로(/api/daily-logs/{date}/task-logs)에서 오고,
// 어떤 업무의 메모인지는 여기 taskId로 받는다.
public record TaskLogRequest(

		@NotNull(message = "업무를 선택해 주세요.")
		Long taskId,

		@NotBlank(message = "내용을 입력해 주세요.")
		String content,

		@Min(value = 0, message = "소요 시간은 0 이상이어야 합니다.")
		Integer spentMinutes

) {
}
