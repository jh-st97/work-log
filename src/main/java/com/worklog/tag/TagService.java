package com.worklog.tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worklog.common.exception.BusinessException;
import com.worklog.common.exception.ErrorCode;
import com.worklog.member.Member;
import com.worklog.member.MemberRepository;
import com.worklog.tag.dto.TagRequest;
import com.worklog.tag.dto.TagResponse;
import com.worklog.task.TaskTagRepository;

@Service
public class TagService {

	private final TagRepository tagRepository;
	private final MemberRepository memberRepository;
	private final TaskTagRepository taskTagRepository;

	public TagService(TagRepository tagRepository, MemberRepository memberRepository,
			TaskTagRepository taskTagRepository) {
		this.tagRepository = tagRepository;
		this.memberRepository = memberRepository;
		this.taskTagRepository = taskTagRepository;
	}

	// 내 태그 목록 조회 (페이징)
	public Page<TagResponse> getTags(Long memberId, Pageable pageable) {
		return tagRepository.findByMemberId(memberId, pageable)
				.map(TagResponse::from);
	}

	// 태그 등록
	@Transactional
	public TagResponse createTag(Long memberId, TagRequest request) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		// DB의 UNIQUE 제약을 어기기 전에 미리 확인해서, 더 친절한 에러 메시지를 준다
		if (tagRepository.existsByMemberIdAndName(memberId, request.name())) {
			throw new BusinessException(ErrorCode.TAG_DUPLICATED);
		}

		Tag tag = new Tag(member, request.name());
		Tag saved = tagRepository.save(tag);
		return TagResponse.from(saved);
	}

	// 태그 이름 수정
	@Transactional
	public TagResponse updateTag(Long id, Long memberId, TagRequest request) {
		Tag tag = findMyTag(id, memberId);

		// 자기 자신은 비교 대상에서 빼고 중복 확인
		if (tagRepository.existsByMemberIdAndNameAndIdNot(memberId, request.name(), id)) {
			throw new BusinessException(ErrorCode.TAG_DUPLICATED);
		}

		tag.rename(request.name());
		return TagResponse.from(tag);
	}

	// 태그 삭제 (보관 처리 없이 진짜로 삭제).
	// 업무에 붙어 있던 태그면 연결(task_tag)부터 끊고 지운다 — 업무 자체는 그대로 남는다.
	// 연결을 안 끊고 지우면 DB의 FK 제약에 걸려 500 에러가 난다.
	@Transactional
	public void deleteTag(Long id, Long memberId) {
		Tag tag = findMyTag(id, memberId);
		taskTagRepository.deleteByTagId(id);
		taskTagRepository.flush();
		tagRepository.delete(tag);
	}

	// 태그를 찾고, 진짜 내 것인지 확인하는 공통 로직
	private Tag findMyTag(Long id, Long memberId) {
		Tag tag = tagRepository.findByIdAndMemberId(id, memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));

		return tag;
	}

}
