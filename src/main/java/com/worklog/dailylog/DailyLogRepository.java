package com.worklog.dailylog;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {

	// 날짜 하나로 조회 (PUT 저장, GET 단일 날짜 조회 둘 다 여기서 씀)
	Optional<DailyLog> findByMemberIdAndLogDate(Long memberId, LocalDate logDate);

	// 기간(from~to) 목록 조회. Pageable을 받으면 Spring Data가 페이징·정렬을
	// 알아서 SQL의 LIMIT/OFFSET, ORDER BY로 바꿔준다 — 직접 페이지 계산 안 해도 됨
	Page<DailyLog> findByMemberIdAndLogDateBetween(Long memberId, LocalDate from, LocalDate to, Pageable pageable);

}
