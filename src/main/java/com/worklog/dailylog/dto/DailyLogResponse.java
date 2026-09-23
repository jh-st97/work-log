package com.worklog.dailylog.dto;

import java.time.LocalDate;
import java.util.List;

import com.worklog.dailylog.DailyLog;
import com.worklog.tasklog.dto.TaskLogResponse;

public record DailyLogResponse(Long id, LocalDate logDate, String summary, List<TaskLogResponse> taskLogs) {

	// 기간별 목록 조회처럼 진행 메모까지는 필요 없을 때 — taskLogs는 빈 목록으로 둔다
	// (일지마다 진행 메모를 다 불러오면 비용이 커져서 목록 조회에서는 뺀다)
	public static DailyLogResponse from(DailyLog dailyLog) {
		return new DailyLogResponse(dailyLog.getId(), dailyLog.getLogDate(), dailyLog.getSummary(), List.of());
	}

	// 하루 일지 상세 조회처럼 진행 메모까지 같이 보여줄 때
	public static DailyLogResponse of(DailyLog dailyLog, List<TaskLogResponse> taskLogs) {
		return new DailyLogResponse(dailyLog.getId(), dailyLog.getLogDate(), dailyLog.getSummary(), taskLogs);
	}

}
