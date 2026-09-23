package com.worklog.task;

import com.worklog.common.BaseEntity;
import com.worklog.worksystem.WorkSystem;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// TaskTag와 같은 구조. 업무-업무시스템을 잇는 중간 엔티티.
@Entity
@Table(name = "task_work_system")
public class TaskWorkSystem extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "task_id", nullable = false)
	private Task task;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "work_system_id", nullable = false)
	private WorkSystem workSystem;

	protected TaskWorkSystem() {
	}

	public TaskWorkSystem(Task task, WorkSystem workSystem) {
		this.task = task;
		this.workSystem = workSystem;
	}

	public Task getTask() {
		return task;
	}

	public WorkSystem getWorkSystem() {
		return workSystem;
	}

}
