package com.worklog.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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

	// 존재하지 않는 주소. 스프링은 못 찾은 주소를 "정적 파일 요청"으로 보고 이 예외를 던진다.
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException e) {
		return ResponseEntity
				.status(ErrorCode.RESOURCE_NOT_FOUND.getStatus())
				.body(ErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND));
	}

	// 주소는 맞는데 방식(GET/POST 등)이 틀린 경우 (예: GET만 있는 곳에 POST)
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
		return ResponseEntity
				.status(ErrorCode.METHOD_NOT_ALLOWED.getStatus())
				.body(ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED));
	}

	// 클라이언트가 보낸 값이 잘못된 경우: JSON이 깨졌거나(HttpMessageNotReadable),
	// 숫자 자리에 글자·잘못된 날짜 형식이 온 경우(MethodArgumentTypeMismatch). 둘 다 400.
	@ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
	public ResponseEntity<ErrorResponse> handleBadRequest(Exception e) {
		return ResponseEntity
				.status(ErrorCode.INVALID_INPUT.getStatus())
				.body(ErrorResponse.of(ErrorCode.INVALID_INPUT));
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
