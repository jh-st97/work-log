package com.worklog.tasklog;

import java.time.LocalDate;
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
import org.springframework.web.bind.annotation.RestController;

import com.worklog.tasklog.dto.TaskLogRequest;
import com.worklog.tasklog.dto.TaskLogResponse;
import com.worklog.tasklog.dto.TaskLogUpdateRequest;

import jakarta.validation.Valid;

// 진행 메모는 일일 기록 밑(등록), 단독 경로(수정·삭제), 업무 밑(조회) 이렇게 세 군데
// 서로 다른 주소를 써서, 클래스 레벨 @RequestMapping 없이 메서드마다 전체 경로를 적는다.
@RestController
public class TaskLogController {

	private final TaskLogService taskLogService;

	public TaskLogController(TaskLogService taskLogService) {
		this.taskLogService = taskLogService;
	}

	// POST /api/daily-logs/{date}/task-logs
	@PostMapping("/api/daily-logs/{date}/task-logs")
	public ResponseEntity<TaskLogResponse> createTaskLog(@AuthenticationPrincipal Long memberId,
			@PathVariable LocalDate date, @Valid @RequestBody TaskLogRequest request) {
		TaskLogResponse response = taskLogService.createTaskLog(memberId, date, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// PATCH /api/task-logs/{id}
	@PatchMapping("/api/task-logs/{id}")
	public TaskLogResponse updateTaskLog(@AuthenticationPrincipal Long memberId, @PathVariable Long id,
			@Valid @RequestBody TaskLogUpdateRequest request) {
		return taskLogService.updateTaskLog(id, memberId, request);
	}

	// DELETE /api/task-logs/{id}
	@DeleteMapping("/api/task-logs/{id}")
	public ResponseEntity<Void> deleteTaskLog(@AuthenticationPrincipal Long memberId, @PathVariable Long id) {
		taskLogService.deleteTaskLog(id, memberId);
		return ResponseEntity.noContent().build();
	}

	// GET /api/tasks/{taskId}/task-logs
	@GetMapping("/api/tasks/{taskId}/task-logs")
	public List<TaskLogResponse> getTaskLogsByTask(@AuthenticationPrincipal Long memberId,
			@PathVariable Long taskId) {
		return taskLogService.getTaskLogsByTask(taskId, memberId);
	}

}
