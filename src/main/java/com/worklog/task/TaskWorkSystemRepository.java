package com.worklog.task;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskWorkSystemRepository extends JpaRepository<TaskWorkSystem, Long> {

	// 이 업무에 연결된 업무 시스템 목록 조회
	List<TaskWorkSystem> findByTaskId(Long taskId);

	// 업무 목록 조회용 — 여러 업무의 업무 시스템을 한 번에 가져온다 (N+1 방지)
	List<TaskWorkSystem> findByTaskIdIn(List<Long> taskIds);

	// 업무 수정 시 업무 시스템을 통째로 다시 붙이기 위해, 기존 연결을 전부 지운다
	void deleteByTaskId(Long taskId);

	// 업무 시스템을 삭제하기 전에, 이 시스템이 달려 있던 모든 업무에서 연결만 끊는다
	void deleteByWorkSystemId(Long workSystemId);

}
