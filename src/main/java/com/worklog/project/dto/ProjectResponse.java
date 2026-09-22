package com.worklog.project.dto;

import java.time.LocalDate;

import com.worklog.project.Project;

public record ProjectResponse(
		Long id,
		String name,
		String description,
		LocalDate startDate,
		LocalDate endDate) {
	
	
	public static ProjectResponse from(Project project) {
		return new ProjectResponse(
				project.getId(),
				project.getName(),
				project.getDescription(),
				project.getStartDate(),
				project.getEndDate());
	}

}
