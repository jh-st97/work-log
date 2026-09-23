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

import com.worklog.task.dto.TaskRequest;
import com.worklog.task.dto.TaskResponse;
import com.worklog.task.dto.TaskStatusRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

	private final TaskService taskService;

	public TaskController(TaskService taskService) {
		this.taskService = taskService;
	}

	// GET /api/tasks : 내 업무 목록
	@GetMapping
	public List<TaskResponse> getTasks(@AuthenticationPrincipal Long memberId) {
		return taskService.getTasks(memberId);
	}

	// GET /api/tasks/{id} : 업무 상세
	@GetMapping("/{id}")
	public TaskResponse getTask(@PathVariable Long id, @AuthenticationPrincipal Long memberId) {
		return taskService.getTask(id, memberId);
	}

	// POST /api/tasks : 업무 등록
	@PostMapping
	public ResponseEntity<TaskResponse> createTask(@AuthenticationPrincipal Long memberId,
			@Valid @RequestBody TaskRequest request) {
		TaskResponse response = taskService.createTask(memberId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// PATCH /api/tasks/{id} : 업무 수정 (상태는 여기서 안 바꿈)
	@PatchMapping("/{id}")
	public TaskResponse updateTask(@PathVariable Long id, @AuthenticationPrincipal Long memberId,
			@Valid @RequestBody TaskRequest request) {
		return taskService.updateTask(id, memberId, request);
	}

	// PATCH /api/tasks/{id}/status : 상태만 따로 변경
	@PatchMapping("/{id}/status")
	public TaskResponse changeStatus(@PathVariable Long id, @AuthenticationPrincipal Long memberId,
			@Valid @RequestBody TaskStatusRequest request) {
		return taskService.changeStatus(id, memberId, request);
	}

	// DELETE /api/tasks/{id} : 실제 삭제가 아니라 보관 처리
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> archiveTask(@PathVariable Long id, @AuthenticationPrincipal Long memberId) {
		taskService.archiveTask(id, memberId);
		return ResponseEntity.noContent().build();
	}

}
