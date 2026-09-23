package com.worklog.dailylog;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.worklog.dailylog.dto.DailyLogRequest;
import com.worklog.dailylog.dto.DailyLogResponse;

@RestController
@RequestMapping("/api/daily-logs")
public class DailyLogController {

	private final DailyLogService dailyLogService;

	public DailyLogController(DailyLogService dailyLogService) {
		this.dailyLogService = dailyLogService;
	}

	// GET /api/daily-logs?from=2026-09-01&to=2026-09-30&page=0&size=20
	@GetMapping
	public Page<DailyLogResponse> getDailyLogs(@AuthenticationPrincipal Long memberId,
			@RequestParam LocalDate from, @RequestParam LocalDate to,
			@PageableDefault(size = 20, sort = "logDate", direction = Sort.Direction.DESC) Pageable pageable) {
		return dailyLogService.getDailyLogs(memberId, from, to, pageable);
	}

	// GET /api/daily-logs/{date}
	@GetMapping("/{date}")
	public DailyLogResponse getDailyLog(@AuthenticationPrincipal Long memberId, @PathVariable LocalDate date) {
		return dailyLogService.getDailyLog(memberId, date);
	}

	// PUT /api/daily-logs/{date} — 없으면 생성, 있으면 수정
	@PutMapping("/{date}")
	public DailyLogResponse saveDailyLog(@AuthenticationPrincipal Long memberId, @PathVariable LocalDate date,
			@RequestBody DailyLogRequest request) {
		return dailyLogService.saveDailyLog(memberId, date, request);
	}

}
