package com.worklog.dailylog.dto;

import java.time.LocalDate;

import com.worklog.dailylog.DailyLog;

// TaskLog(진행 메모)는 다음 단계에서 붙는다. 지금은 회고만 담는다.
public record DailyLogResponse(Long id, LocalDate logDate, String summary) {

	public static DailyLogResponse from(DailyLog dailyLog) {
		return new DailyLogResponse(dailyLog.getId(), dailyLog.getLogDate(), dailyLog.getSummary());
	}

}
