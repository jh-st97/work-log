package com.worklog.tag;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

	// 내 태그 전체 조회 (태그는 보관 개념이 없어서 archivedAt 조건 없음)
	List<Tag> findByMemberId(Long memberId);

	// 태그 번호 + 회원 번호를 같이 확인해서, 남의 태그를 못 만지게 막는 용도
	Optional<Tag> findByIdAndMemberId(Long id, Long memberId);

	// 새 태그 등록 시 이름 중복 확인용 (이메일 중복 확인과 같은 패턴)
	boolean existsByMemberIdAndName(Long memberId, String name);

	// 태그 이름 수정 시 중복 확인용. 자기 자신(id)은 비교 대상에서 빼야
	// "원래 이름 그대로 수정"할 때도 중복으로 잘못 걸리지 않는다
	boolean existsByMemberIdAndNameAndIdNot(Long memberId, String name, Long id);

}
