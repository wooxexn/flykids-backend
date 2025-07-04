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

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKeyString;

    private SecretKey secretKey;

    private static final long ACCESS_TOKEN_EXPIRATION = 60 * 60 * 1000L; // 1시간
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L; // 7일

    private static final String BEARER_PREFIX = "Bearer ";

    @PostConstruct
    protected void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKeyString);
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // 액세스 토큰 생성
    public String createAccessToken(String username, String role) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("role", role);
        claims.put("type", "access"); // 토큰 타입 명시

        Date now = new Date();
        Date expiry = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 리프레시 토큰 생성
    public String createRefreshToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("type", "refresh"); // 토큰 타입 명시

        Date now = new Date();
        Date expiry = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰에서 사용자 정보 추출
    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    // 토큰에서 권한 추출
    public String getUserRole(String token) {
        return (String) getClaims(token).get("role");
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 실제 Claims 반환
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // HTTP 요청에서 헤더에 담긴 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * 토큰 타입 추출
     * 액세스 토큰인지, 리프레시 토큰인지 확인
     */
    public String getTokenType(String token) {
        return (String) getClaims(token).get("type");
    }
}
