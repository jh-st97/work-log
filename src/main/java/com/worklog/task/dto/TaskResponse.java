package com.worklog.task.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.worklog.tag.dto.TagResponse;
import com.worklog.task.Task;
import com.worklog.task.TaskPriority;
import com.worklog.task.TaskStatus;
import com.worklog.worksystem.dto.WorkSystemResponse;

public record TaskResponse(
		Long id,
		Long projectId,
		String title,
		String description,
		TaskStatus status,
		TaskPriority priority,
		LocalDate dueDate,
		LocalDateTime completedAt,
		List<TagResponse> tags,
		List<WorkSystemResponse> systems) {

	// 태그·시스템 목록은 서비스에서 따로 조회해서 넘겨준다 (Task 엔티티 혼자서는 모름)
	public static TaskResponse of(Task task, List<TagResponse> tags, List<WorkSystemResponse> systems) {
		return new TaskResponse(
				task.getId(),
				task.getProject().getId(),
				task.getTitle(),
				task.getDescription(),
				task.getStatus(),
				task.getPriority(),
				task.getDueDate(),
				task.getCompletedAt(),
				tags,
				systems);
	}

}
