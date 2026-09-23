package com.worklog.task;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worklog.common.exception.BusinessException;
import com.worklog.common.exception.ErrorCode;
import com.worklog.task.dto.TaskResultRequest;
import com.worklog.task.dto.TaskResultResponse;

@Service
public class TaskResultService {

	private final TaskResultRepository taskResultRepository;
	private final TaskRepository taskRepository;

	public TaskResultService(TaskResultRepository taskResultRepository, TaskRepository taskRepository) {
		this.taskResultRepository = taskResultRepository;
		this.taskRepository = taskRepository;
	}

	@Transactional
	public TaskResultResponse createResult(Long taskId, Long memberId, TaskResultRequest request) {
		// 성과 항목을 붙이려는 업무가 진짜 내 것인지 먼저 확인
		Task task = findMyTask(taskId, memberId);

		TaskResult result = new TaskResult(task, request.metricName(), request.beforeValue(), request.afterValue());
		TaskResult saved = taskResultRepository.save(result);

		return TaskResultResponse.from(saved);
	}

	@Transactional
	public TaskResultResponse updateResult(Long taskId, Long resultId, Long memberId, TaskResultRequest request) {
		findMyTask(taskId, memberId);
		TaskResult result = findResultInTask(resultId, taskId);

		result.update(request.metricName(), request.beforeValue(), request.afterValue());

		return TaskResultResponse.from(result);
	}

	@Transactional
	public void deleteResult(Long taskId, Long resultId, Long memberId) {
		findMyTask(taskId, memberId);
		TaskResult result = findResultInTask(resultId, taskId);

		taskResultRepository.delete(result);
	}

	// TaskService에 있는 것과 같은 확인 로직. 업무가 존재하고, 내 것인지 확인한다.
	private Task findMyTask(Long taskId, Long memberId) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

		if (!task.getMember().getId().equals(memberId)) {
			throw new BusinessException(ErrorCode.TASK_ACCESS_DENIED);
		}

		return task;
	}

	// 성과 항목이 존재하고, 진짜 이 업무에 속한 게 맞는지 확인한다.
	private TaskResult findResultInTask(Long resultId, Long taskId) {
		return taskResultRepository.findByIdAndTaskId(resultId, taskId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TASK_RESULT_NOT_FOUND));
	}

}
