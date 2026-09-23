package com.worklog.task.dto;

import com.worklog.task.TaskResult;

public record TaskResultResponse(Long id, String metricName, String beforeValue, String afterValue) {

	public static TaskResultResponse from(TaskResult result) {
		return new TaskResultResponse(result.getId(), result.getMetricName(),
				result.getBeforeValue(), result.getAfterValue());
	}

}
