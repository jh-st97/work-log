package com.worklog.task.dto;

import com.worklog.task.TaskStatus;

import jakarta.validation.constraints.NotNull;

public record TaskStatusRequest(

		@NotNull(message = "변경할 상태를 선택해 주세요.")
		TaskStatus status

) {
}
