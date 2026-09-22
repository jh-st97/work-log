package com.worklog.worksystem;

import com.worklog.common.BaseEntity;
import com.worklog.member.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "work_system")
public class WorkSystem extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	protected WorkSystem() {
	}

	public WorkSystem(Member member, String name, String description) {
		this.member = member;
		this.name = name;
		this.description = description;
	}

	public Member getMember() {
		return member;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	// Tag와 마찬가지로 보관 처리 없음. 이름과 설명만 바꾼다.
	public void update(String name, String description) {
		this.name = name;
		this.description = description;
	}

}
