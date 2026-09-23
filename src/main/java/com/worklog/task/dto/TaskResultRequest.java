package com.worklog.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskResultRequest(

		@NotBlank(message = "지표명을 입력해 주세요.")
		@Size(max = 100, message = "지표명은 100자 이하여야 합니다.")
		String metricName,

		// 개선 전 값은 없을 수도 있다 (기획서: 수치 아닌 성과도 담을 수 있음)
		String beforeValue,

		@NotBlank(message = "개선 후 값을 입력해 주세요.")
		@Size(max = 200, message = "개선 후 값은 200자 이하여야 합니다.")
		String afterValue

) {
}
