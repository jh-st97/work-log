package com.worklog.project.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectRequest(
		@NotBlank(message = "프로젝트 이름을 입력해 주세요.")
		@Size(max = 100, message = "프로젝트 이름은 100자 이하여야 합니다.")
		String name,

		String description,

		LocalDate startDate,

		LocalDate endDate
		) {

}
