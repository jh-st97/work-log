package com.worklog.task;

// 업무 상태. DB에는 문자열("TODO" 등)로 저장된다 (기획서 규칙).
public enum TaskStatus {
	TODO,
	IN_PROGRESS,
	DONE
}
