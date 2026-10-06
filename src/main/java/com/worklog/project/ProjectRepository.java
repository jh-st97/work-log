package com.worklog.project;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

	// 내 프로젝트 중 보관 안 된 것을 페이지 단위로 조회
	Page<Project> findByMemberIdAndArchivedAtIsNull(Long memberId, Pageable pageable);
	
	Optional<Project> findByIdAndMemberId(Long id, Long memberId);
	

}
