package com.worklog.tag;

import com.worklog.common.BaseEntity;
import com.worklog.member.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tag")
public class Tag extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Column(nullable = false, length = 50)
	private String name;

	protected Tag() {
	}

	public Tag(Member member, String name) {
		this.member = member;
		this.name = name;
	}

	public Member getMember() {
		return member;
	}

	public String getName() {
		return name;
	}

	// 태그는 보관 처리가 없다 (기획서: archived_at은 프로젝트·업무에만 적용). 이름만 바꾸면 된다.
	public void rename(String name) {
		this.name = name;
	}

}
