package com.mtvs.flykidsbackend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증 유틸리티 클래스
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKeyString;

    @Value("${jwt.token-validity-in-seconds:3600}") // Access Token 만료 시간 (초)
    private long accessTokenValiditySeconds;

    @Value("${jwt.refresh-token-validity-in-seconds:604800}") // Refresh Token 만료 시간 (초)
    private long refreshTokenValiditySeconds;

    private SecretKey secretKey;

    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Base64 인코딩된 시크릿 키를 실제 키 객체로 초기화
     */
    @PostConstruct
    protected void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKeyString);
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 액세스 토큰 생성
     * @param username 사용자 이름
     * @param role 사용자 권한
     * @return JWT 액세스 토큰 문자열
     */
    public String createAccessToken(String username, String role) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("role", role);
        claims.put("type", "access");

        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValiditySeconds * 1000L);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 리프레시 토큰 생성
     * @param username 사용자 이름
     * @return JWT 리프레시 토큰 문자열
     */
    public String createRefreshToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("type", "refresh");

        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValiditySeconds * 1000L);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰에서 사용자명 추출
     */
    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * 토큰에서 사용자 권한 추출
     */
    public String getUserRole(String token) {
        return (String) getClaims(token).get("role");
    }

    /**
     * 토큰에서 타입 추출 (access / refresh)
     */
    public String getTokenType(String token) {
        return (String) getClaims(token).get("type");
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 토큰의 Claims 객체 추출
     */
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * HTTP 요청에서 Bearer 토큰 추출
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
