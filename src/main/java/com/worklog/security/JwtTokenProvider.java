package com.worklog.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {
	
	private static final long EXPIRATION_MILLIS = 1000L * 60 * 60 * 2; // 2시간
	
	private final Key key;
	
	public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes());
	}
	
	// 토큰만들기
	public String createToken(Long memberId) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + EXPIRATION_MILLIS);
		
		return Jwts.builder()
				.subject(String.valueOf(memberId))
				.issuedAt(now)
				.expiration(expiry)
				.signWith(key)
				.compact();
	}
	
	// 토큰에서 회원 번호 꺼내기
	public Long getMemberId(String token) {
		Claims claims = Jwts.parser()
				.verifyWith((javax.crypto.SecretKey) key)
				.build()
				.parseSignedClaims(token)
				.getPayload();

		return Long.valueOf(claims.getSubject());
	}
	
	// 토큰이 유효한지 확인
	public boolean isValid(String token) {
		try {
			Jwts.parser()
					.verifyWith((javax.crypto.SecretKey) key)
					.build()
					.parseSignedClaims(token);
			return true;
		} catch (ExpiredJwtException e) {
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	

}
