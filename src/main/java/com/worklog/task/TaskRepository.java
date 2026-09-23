package com.worklog.task;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long>{
	
	// 내 업무 중 보관 안 된 것만
	List<Task> findByMemberIdAndArchivedAtIsNull(Long memberId);
	
	// 업무 번호 + 회원 번호로 소유권까지 같이 확인
	Optional<Task> findByIdAndMemberId(Long id, Long memberId);

}
