package com.worklog.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
		ErrorCode errorCode = e.getErrorCode();
		return ResponseEntity
				.status(errorCode.getStatus())
				.body(ErrorResponse.of(errorCode));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(FieldError::getDefaultMessage)
				.orElse(ErrorCode.INVALID_INPUT.getMessage());

		return ResponseEntity
				.status(ErrorCode.INVALID_INPUT.getStatus())
				.body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message));
	}

	// 필수 @RequestParam이 빠졌을 때 (예: /api/daily-logs 조회에 from/to 없이 요청).
	// 이걸 따로 안 잡으면 아래 catch-all로 떨어져서 500이 되는데, 이건 클라이언트 잘못(검증 실패)이라 400이 맞다.
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException e) {
		String message = "필수 파라미터가 없습니다: " + e.getParameterName();

		return ResponseEntity
				.status(ErrorCode.INVALID_INPUT.getStatus())
				.body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message));
	}

	// 위에서 처리하지 못한 나머지 모든 예외. 이게 없으면 예외가 그대로 흘러나가서
	// (이 앱의 Security 설정 특성상) 엉뚱하게 401로 보였다 — 원인 파악을 어렵게 만드는 문제라
	// 최소한 500으로는 정직하게 보이게 한다.
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e) {
		return ResponseEntity
				.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
				.body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
	}

}
