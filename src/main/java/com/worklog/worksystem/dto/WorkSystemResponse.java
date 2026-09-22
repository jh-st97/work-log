package com.worklog.worksystem.dto;

import com.worklog.worksystem.WorkSystem;

public record WorkSystemResponse(Long id, String name, String description) {

	public static WorkSystemResponse from(WorkSystem workSystem) {
		return new WorkSystemResponse(workSystem.getId(), workSystem.getName(), workSystem.getDescription());
	}

}
