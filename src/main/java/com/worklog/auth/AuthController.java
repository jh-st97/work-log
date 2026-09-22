package com.worklog.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.worklog.member.MemberService;
import com.worklog.member.dto.SignupRequest;
import com.worklog.member.dto.SignupResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
	private final MemberService memberService;
	
	public AuthController(MemberService memberService) {
		this.memberService = memberService;
	}
	
	@PostMapping("/signup")
	public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
		SignupResponse response = memberService.signup(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

}
