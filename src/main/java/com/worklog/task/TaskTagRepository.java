package com.worklog.task;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskTagRepository extends JpaRepository<TaskTag, Long> {
	
	// 이 업무에 연결된 태그 목록 조회
	List<TaskTag> findByTaskId(Long taskId);

	// 업무 목록 조회용 — 여러 업무의 태그를 한 번에 가져온다 (N+1 방지)
	List<TaskTag> findByTaskIdIn(List<Long> taskIds);

	// 업무 수정 시 태그를 통째로 다시 붙이기 위해, 기존 연결을 전부 지운다
	void deleteByTaskId(Long taskId);

	// 태그를 삭제하기 전에, 이 태그가 달려 있던 모든 업무에서 연결만 끊는다
	void deleteByTagId(Long tagId);
	

}
