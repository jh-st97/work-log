package com.worklog.member;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worklog.common.exception.BusinessException;
import com.worklog.common.exception.ErrorCode;
import com.worklog.member.dto.SignupRequest;
import com.worklog.member.dto.SignupResponse;

@Service
public class MemberService {
	
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	
	
	public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
		this.memberRepository = memberRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	@Transactional
	public SignupResponse signup(SignupRequest request) {
		if (memberRepository.existsByEmail(request.email())) {
			throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
		}
		
		Member member = new Member(
				request.email(),
				passwordEncoder.encode(request.password()),
				request.nickname());
		
		Member saved = memberRepository.save(member);					
		return SignupResponse.from(saved);
		
	}

}
