package com.worklog.member;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worklog.common.exception.BusinessException;
import com.worklog.common.exception.ErrorCode;
import com.worklog.member.dto.LoginRequest;
import com.worklog.member.dto.LoginResponse;
import com.worklog.member.dto.SignupRequest;
import com.worklog.member.dto.SignupResponse;
import com.worklog.security.JwtTokenProvider;

@Service
public class MemberService {
	
	private final JwtTokenProvider jwtTokenProvider;
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	
	
	public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
		this.memberRepository = memberRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
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
	
	public LoginResponse login(LoginRequest request) {
		Member member = memberRepository.findByEmail(request.email())
				.orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
		
		if (!passwordEncoder.matches(request.password(), member.getPassword())) {
			throw new BusinessException(ErrorCode.LOGIN_FAILED);
		}
		
		String accessToken = jwtTokenProvider.createToken(member.getId());
		return new LoginResponse(accessToken);
		
	}

}
