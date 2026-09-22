package com.worklog.worksystem;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.worklog.worksystem.dto.WorkSystemRequest;
import com.worklog.worksystem.dto.WorkSystemResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/systems")
public class WorkSystemController {

	private final WorkSystemService workSystemService;

	public WorkSystemController(WorkSystemService workSystemService) {
		this.workSystemService = workSystemService;
	}

	// GET /api/systems : 내 업무 시스템 목록
	@GetMapping
	public List<WorkSystemResponse> getWorkSystems(@AuthenticationPrincipal Long memberId) {
		return workSystemService.getWorkSystems(memberId);
	}

	// POST /api/systems : 업무 시스템 등록
	@PostMapping
	public ResponseEntity<WorkSystemResponse> createWorkSystem(@AuthenticationPrincipal Long memberId,
			@Valid @RequestBody WorkSystemRequest request) {
		WorkSystemResponse response = workSystemService.createWorkSystem(memberId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// PATCH /api/systems/{id} : 업무 시스템 수정
	@PatchMapping("/{id}")
	public WorkSystemResponse updateWorkSystem(@PathVariable Long id, @AuthenticationPrincipal Long memberId,
			@Valid @RequestBody WorkSystemRequest request) {
		return workSystemService.updateWorkSystem(id, memberId, request);
	}

	// DELETE /api/systems/{id} : 진짜 삭제 (보관 처리 아님)
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteWorkSystem(@PathVariable Long id, @AuthenticationPrincipal Long memberId) {
		workSystemService.deleteWorkSystem(id, memberId);
		return ResponseEntity.noContent().build();
	}

}
