package com.worklog.tasklog;

import com.worklog.common.BaseEntity;
import com.worklog.dailylog.DailyLog;
import com.worklog.task.Task;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "task_log")
public class TaskLog extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "daily_log_id", nullable = false)
	private DailyLog dailyLog;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "task_id", nullable = false)
	private Task task;

	// 겪은 문제와 해결 방법
	private String content;

	// 소요 시간(분). NULL 허용
	private Integer spentMinutes;

	protected TaskLog() {
	}

	public TaskLog(DailyLog dailyLog, Task task, String content, Integer spentMinutes) {
		this.dailyLog = dailyLog;
		this.task = task;
		this.content = content;
		this.spentMinutes = spentMinutes;
	}

	// 업무는 등록 후 바꿀 수 없다 — 내용과 소요 시간만 수정 대상
	public void update(String content, Integer spentMinutes) {
		this.content = content;
		this.spentMinutes = spentMinutes;
	}

	public DailyLog getDailyLog() {
		return dailyLog;
	}

	public Task getTask() {
		return task;
	}

	public String getContent() {
		return content;
	}

	public Integer getSpentMinutes() {
		return spentMinutes;
	}

}
