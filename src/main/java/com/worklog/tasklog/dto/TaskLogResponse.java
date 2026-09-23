package com.worklog.tasklog.dto;

import java.time.LocalDate;

import com.worklog.tasklog.TaskLog;

// 하루 일지 화면(업무 제목이 필요)과 업무별 조회 화면(날짜가 필요) 둘 다에서
// 재사용하려고 taskId·taskTitle·logDate를 전부 담는다.
public record TaskLogResponse(
		Long id,
		Long taskId,
		String taskTitle,
		LocalDate logDate,
		String content,
		Integer spentMinutes) {

	public static TaskLogResponse from(TaskLog taskLog) {
		return new TaskLogResponse(
				taskLog.getId(),
				taskLog.getTask().getId(),
				taskLog.getTask().getTitle(),
				taskLog.getDailyLog().getLogDate(),
				taskLog.getContent(),
				taskLog.getSpentMinutes());
	}

}
