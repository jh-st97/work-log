package com.worklog.project;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.worklog.common.BaseEntity;
import com.worklog.member.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "project")
public class Project extends BaseEntity{
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	private LocalDate startDate;

	private LocalDate endDate;

	private LocalDateTime archivedAt;
	
	protected Project() {
		
	}

	public Project(Member member, String name, String description, LocalDate startDate, LocalDate endDate) {

		this.member = member;
		this.name = name;
		this.description = description;
		this.startDate = startDate;
		this.endDate = endDate;
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

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public LocalDateTime getArchivedAt() {
		return archivedAt;
	}

	public boolean isArchived() {
		return archivedAt != null;
	}

	public void update(String name, String description, LocalDate startDate, LocalDate endDate) {
		this.name = name;
		this.description = description;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public void archive() {
		this.archivedAt = LocalDateTime.now();
	}
	
	

}
