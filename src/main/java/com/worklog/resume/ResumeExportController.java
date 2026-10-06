package com.worklog.resume;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.worklog.resume.dto.ResumeExportResponse;

@RestController
@RequestMapping("/api/exports")
public class ResumeExportController {

	private final ResumeExportService resumeExportService;

	public ResumeExportController(ResumeExportService resumeExportService) {
		this.resumeExportService = resumeExportService;
	}

	// GET /api/exports/resume?taskIds=1&taskIds=2 : 고른 업무들을 이력서용 마크다운으로
	@GetMapping("/resume")
	public ResumeExportResponse exportResume(@AuthenticationPrincipal Long memberId,
			@RequestParam List<Long> taskIds) {
		return resumeExportService.exportResume(memberId, taskIds);
	}

}
