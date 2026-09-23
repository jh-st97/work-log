package com.worklog.task;

import com.worklog.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// 업무 하나에 성과 항목 여러 개가 붙는다 (Task 1:N TaskResult).
// 보관 개념 없음 — 지우면 진짜로 지워진다 (Tag와 같은 방식).
@Entity
@Table(name = "task_result")
public class TaskResult extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "task_id", nullable = false)
	private Task task;

	@Column(nullable = false, length = 100)
	private String metricName;

	// 개선 전 값은 없을 수도 있다 (기획서: 수치 아닌 성과도 담을 수 있음)
	@Column(length = 200)
	private String beforeValue;

	@Column(nullable = false, length = 200)
	private String afterValue;

	protected TaskResult() {
	}

	public TaskResult(Task task, String metricName, String beforeValue, String afterValue) {
		this.task = task;
		this.metricName = metricName;
		this.beforeValue = beforeValue;
		this.afterValue = afterValue;
	}

	public Task getTask() {
		return task;
	}

	public String getMetricName() {
		return metricName;
	}

	public String getBeforeValue() {
		return beforeValue;
	}

	public String getAfterValue() {
		return afterValue;
	}

	public void update(String metricName, String beforeValue, String afterValue) {
		this.metricName = metricName;
		this.beforeValue = beforeValue;
		this.afterValue = afterValue;
	}

}
