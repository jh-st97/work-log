package com.worklog.dailylog.dto;

// summary는 NULL 허용(기획서 규칙)이라 @NotBlank 같은 검증을 안 붙인다
public record DailyLogRequest(String summary) {
}
