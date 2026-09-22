package com.worklog.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
	
	EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
	LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."), 
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
	
	private final HttpStatus status;
	private final String message;
	
	ErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}
	
	public HttpStatus getStatus() {
		return status;
	}
	
	public String getMessage() {
		return message;
	}

}
