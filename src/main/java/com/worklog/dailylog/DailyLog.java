package com.worklog.dailylog;

import java.time.LocalDate;

import com.worklog.common.BaseEntity;
import com.worklog.member.Member;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="daily_log")
public class DailyLog extends BaseEntity{
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	private LocalDate logDate;

	// TEXT 컬럼이라 길이 제한 없음 — @Column(columnDefinition = "TEXT")까지는 안 써도
	// 됨(DB 컬럼 타입이 이미 TEXT라 JPA는 그냥 문자열로 주고받기만 하면 됨)
	private String summary;

	protected DailyLog() {
	}

	public DailyLog(Member member, LocalDate logDate, String summary) {
		this.member = member;
		this.logDate = logDate;
		this.summary = summary;
	}

	// Tag.rename()과 같은 패턴 — 단일 필드만 바꾸는 전용 메서드
	public void updateSummary(String summary) {
		this.summary = summary;
	}

	public LocalDate getLogDate() {
		return logDate;
	}

	public String getSummary() {
		return summary;
	}

	

}
