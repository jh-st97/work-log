package com.worklog.worksystem;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkSystemRepository extends JpaRepository<WorkSystem, Long> {

	// 내 업무 시스템 전체 조회 (보관 개념 없음)
	List<WorkSystem> findByMemberId(Long memberId);

	// 시스템 번호 + 회원 번호를 같이 확인해서, 남의 것을 못 만지게 막는 용도
	Optional<WorkSystem> findByIdAndMemberId(Long id, Long memberId);

	// 새 시스템 등록 시 이름 중복 확인용
	boolean existsByMemberIdAndName(Long memberId, String name);

	// 이름 수정 시 중복 확인용. 자기 자신(id)은 비교 대상에서 뺀다
	boolean existsByMemberIdAndNameAndIdNot(Long memberId, String name, Long id);

}
