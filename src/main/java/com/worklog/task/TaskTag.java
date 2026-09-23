package com.worklog.task;

import com.worklog.common.BaseEntity;
import com.worklog.tag.Tag;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// 업무-태그를 잇는 중간 엔티티. "업무 하나에 태그 여러 개"를 이 행(row) 여러 개로 표현한다.
// (업무1, 태그A), (업무1, 태그B)처럼 한 줄에 하나씩 연결 정보만 담는다.
@Entity
@Table(name = "task_tag")
public class TaskTag extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "task_id", nullable = false)
	private Task task;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tag_id", nullable = false)
	private Tag tag;

	protected TaskTag() {
	}

	public TaskTag(Task task, Tag tag) {
		this.task = task;
		this.tag = tag;
	}

	public Task getTask() {
		return task;
	}

	public Tag getTag() {
		return tag;
	}

}
