package com.mtvs.flykidsbackend.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증을 담당하는 Provider 클래스
 *
 * - 토큰 생성
 * - 토큰에서 사용자 정보 추출
 * - 토큰 유효성 검사
 * - Request 헤더에서 토큰 추출
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey; // application.yml에서 주입

    @Value("${jwt.token-validity-in-seconds}")
    private long tokenValidityInSeconds; // 초 단위

    private Key key; // 암호화에 사용할 키 객체

    /**
     * Bean 초기화 시, Base64로 secretKey 인코딩하고 Key 객체 생성
     */
    @PostConstruct
    protected void init() {
        byte[] keyBytes = Base64.getEncoder().encode(secretKey.getBytes());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * JWT 토큰 생성
     *
     * @param userId 사용자 식별자 (PK 또는 이메일 등)
     * @return 생성된 JWT 토큰 문자열
     */
    public String createToken(String userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + tokenValidityInSeconds * 1000);

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * JWT 토큰에서 사용자 ID 추출
     *
     * @param token JWT 문자열
     * @return 사용자 ID
     */
    public String getUserIdFromToken(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    /**
     * 토큰 유효성 검증
     *
     * @param token 검증할 JWT
     * @return 유효 여부
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다");
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT입니다");
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 JWT입니다");
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 JWT입니다");
        }
        return false;
    }

    /**
     * Authorization 헤더에서 JWT 추출
     *
     * @param request HttpServletRequest
     * @return 토큰 문자열 (Bearer 제거됨)
     */
    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    /**
     * JWT 파싱 (내부적으로 예외 던짐)
     */
    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}
