package com.worklog.member;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.worklog.common.exception.BusinessException;
import com.worklog.common.exception.ErrorCode;
import com.worklog.member.dto.MemberResponse;

@RestController
@RequestMapping("/api/members")
public class MemberController {
	
	private final MemberRepository memberRepository;
	
	public MemberController(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}
	
	@GetMapping("/me")
	public MemberResponse me (@AuthenticationPrincipal Long memberId) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
		
		return MemberResponse.from(member);
	}

}
