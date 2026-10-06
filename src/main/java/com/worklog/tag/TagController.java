package com.worklog.tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.worklog.tag.dto.TagRequest;
import com.worklog.tag.dto.TagResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tags")
public class TagController {

	private final TagService tagService;

	public TagController(TagService tagService) {
		this.tagService = tagService;
	}

	// GET /api/tags : 내 태그 목록 (page, size, sort). 기본은 먼저 만든 순서.
	@GetMapping
	public Page<TagResponse> getTags(@AuthenticationPrincipal Long memberId,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
		return tagService.getTags(memberId, pageable);
	}

	// POST /api/tags : 태그 등록
	@PostMapping
	public ResponseEntity<TagResponse> createTag(@AuthenticationPrincipal Long memberId,
			@Valid @RequestBody TagRequest request) {
		TagResponse response = tagService.createTag(memberId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// PATCH /api/tags/{id} : 태그 이름 수정
	@PatchMapping("/{id}")
	public TagResponse updateTag(@PathVariable Long id, @AuthenticationPrincipal Long memberId,
			@Valid @RequestBody TagRequest request) {
		return tagService.updateTag(id, memberId, request);
	}

	// DELETE /api/tags/{id} : 진짜 삭제 (프로젝트와 달리 보관 처리 아님)
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTag(@PathVariable Long id, @AuthenticationPrincipal Long memberId) {
		tagService.deleteTag(id, memberId);
		return ResponseEntity.noContent().build();
	}

}
