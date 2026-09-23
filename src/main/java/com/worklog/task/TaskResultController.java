package com.worklog.task;

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

import com.worklog.task.dto.TaskResultRequest;
import com.worklog.task.dto.TaskResultResponse;

import jakarta.validation.Valid;

// 업무(task) 밑에 걸려 있는 주소라서, 클래스 레벨 경로에 {taskId}가 들어간다
@RestController
@RequestMapping("/api/tasks/{taskId}/results")
public class TaskResultController {

	private final TaskResultService taskResultService;

	public TaskResultController(TaskResultService taskResultService) {
		this.taskResultService = taskResultService;
	}

	// GET /api/tasks/{taskId}/results — 이 업무에 달린 성과 항목 전체 조회
	@GetMapping
	public List<TaskResultResponse> getResults(@PathVariable Long taskId,
			@AuthenticationPrincipal Long memberId) {
		return taskResultService.getResults(taskId, memberId);
	}

	// POST /api/tasks/{taskId}/results
	@PostMapping
	public ResponseEntity<TaskResultResponse> createResult(@PathVariable Long taskId,
			@AuthenticationPrincipal Long memberId, @Valid @RequestBody TaskResultRequest request) {
		TaskResultResponse response = taskResultService.createResult(taskId, memberId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// PATCH /api/tasks/{taskId}/results/{resultId}
	@PatchMapping("/{resultId}")
	public TaskResultResponse updateResult(@PathVariable Long taskId, @PathVariable Long resultId,
			@AuthenticationPrincipal Long memberId, @Valid @RequestBody TaskResultRequest request) {
		return taskResultService.updateResult(taskId, resultId, memberId, request);
	}

	// DELETE /api/tasks/{taskId}/results/{resultId}
	@DeleteMapping("/{resultId}")
	public ResponseEntity<Void> deleteResult(@PathVariable Long taskId, @PathVariable Long resultId,
			@AuthenticationPrincipal Long memberId) {
		taskResultService.deleteResult(taskId, resultId, memberId);
		return ResponseEntity.noContent().build();
	}

}
