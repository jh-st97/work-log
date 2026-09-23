package com.worklog.task;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.worklog.common.BaseEntity;
import com.worklog.member.Member;
import com.worklog.project.Project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "task")
public class Task extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	// 이 업무가 속한 프로젝트. Project와 마찬가지로 지연 로딩(LAZY)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	// @Enumerated(EnumType.STRING): enum 값을 이름 그대로("TODO") DB에 저장한다.
	// 없으면 JPA가 기본으로 숫자(0,1,2)로 저장하는데, 나중에 enum 순서가 바뀌면
	// 기존 데이터가 틀어지는 위험한 방식이라 항상 STRING을 명시한다.
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TaskStatus status;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private TaskPriority priority;

	private LocalDate dueDate;

	// 완료 상태로 바뀐 시각. changeStatus()에서만 채워진다.
	private LocalDateTime completedAt;

	private LocalDateTime archivedAt;

	protected Task() {
	}

	public Task(Member member, Project project, String title, String description,
			TaskPriority priority, LocalDate dueDate) {
		this.member = member;
		this.project = project;
		this.title = title;
		this.description = description;
		// 새로 만드는 업무는 항상 TODO로 시작 (기획서: 기본값 TODO)
		this.status = TaskStatus.TODO;
		// 우선순위를 안 보내면 기본값 MEDIUM (기획서: 기본값 MEDIUM)
		this.priority = priority != null ? priority : TaskPriority.MEDIUM;
		this.dueDate = dueDate;
	}

	public Member getMember() {
		return member;
	}

	public Project getProject() {
		return project;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public TaskPriority getPriority() {
		return priority;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public LocalDateTime getArchivedAt() {
		return archivedAt;
	}

	public boolean isArchived() {
		return archivedAt != null;
	}

	// 일반 수정 (PATCH /api/tasks/{id}). 상태는 여기서 안 바꾼다 — 상태 변경 API가 따로 있다.
	public void update(Project project, String title, String description, TaskPriority priority, LocalDate dueDate) {
		this.project = project;
		this.title = title;
		this.description = description;
		this.priority = priority;
		this.dueDate = dueDate;
	}

	// 상태 변경 전용 (PATCH /api/tasks/{id}/status).
	// DONE으로 바뀌면 완료일을 기록하고, DONE이 아니게 되면 완료일을 다시 비운다.
	public void changeStatus(TaskStatus status) {
		this.status = status;
		this.completedAt = (status == TaskStatus.DONE) ? LocalDateTime.now() : null;
	}

	public void archive() {
		this.archivedAt = LocalDateTime.now();
	}

}
