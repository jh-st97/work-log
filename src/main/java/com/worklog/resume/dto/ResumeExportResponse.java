package com.worklog.resume.dto;

// 이력서용으로 만든 마크다운 문서 전체를 문자열 하나로 담는다
public record ResumeExportResponse(String markdown) {
}
