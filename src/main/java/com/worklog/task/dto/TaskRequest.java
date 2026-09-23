package com.worklog.task.dto;

import java.time.LocalDate;
import java.util.List;

import com.worklog.task.TaskPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaskRequest(

		@NotBlank(message = "업무 제목을 입력해 주세요.")
		@Size(max = 200, message = "업무 제목은 200자 이하여야 합니다.")
		String title,

		String description,

		// null이면 엔티티 생성자에서 MEDIUM으로 채워진다
		TaskPriority priority,

		LocalDate dueDate,

		@NotNull(message = "프로젝트를 선택해 주세요.")
		Long projectId,

		// 태그를 안 고르면 빈 목록으로 온다고 가정
		List<Long> tagIds,

		List<Long> systemIds

) {
}
