package com.worklog.member;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {			// JpaRepository를 상속받으면 직접만들지 않아도 사용가능
	
	boolean existsByEmail(String email);
	
	Optional<Member> findByEmail(String email);				// WHERE EMAIL = ''
	

}
