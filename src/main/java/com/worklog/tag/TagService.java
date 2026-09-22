package com.worklog.tag;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worklog.common.exception.BusinessException;
import com.worklog.common.exception.ErrorCode;
import com.worklog.member.Member;
import com.worklog.member.MemberRepository;
import com.worklog.tag.dto.TagRequest;
import com.worklog.tag.dto.TagResponse;

@Service
public class TagService {

	private final TagRepository tagRepository;
	private final MemberRepository memberRepository;

	public TagService(TagRepository tagRepository, MemberRepository memberRepository) {
		this.tagRepository = tagRepository;
		this.memberRepository = memberRepository;
	}

	// 내 태그 전체 조회
	public List<TagResponse> getTags(Long memberId) {
		return tagRepository.findByMemberId(memberId)
				.stream()
				.map(TagResponse::from)
				.toList();
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

	// 태그 삭제 (보관 처리 없이 진짜로 삭제)
	@Transactional
	public void deleteTag(Long id, Long memberId) {
		Tag tag = findMyTag(id, memberId);
		tagRepository.delete(tag);
	}

	// 태그를 찾고, 진짜 내 것인지 확인하는 공통 로직
	private Tag findMyTag(Long id, Long memberId) {
		Tag tag = tagRepository.findByIdAndMemberId(id, memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));

		return tag;
	}

}
