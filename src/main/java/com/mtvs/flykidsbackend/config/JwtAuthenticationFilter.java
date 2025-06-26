package com.mtvs.flykidsbackend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 요청마다 JWT를 검사해 인증 정보를 SecurityContext에 저장하는 필터
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // 1) Authorization 헤더에서 토큰 추출
        String token = jwtUtil.resolveToken(request);

        // 2) 토큰 유효 && 현재 인증 정보가 비어 있으면 처리
        if (token != null && jwtUtil.validateToken(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 3) 토큰에서 username, role 추출
            String username = jwtUtil.getUsername(token);
            String role = jwtUtil.getUserRole(token); // 예: "USER" 또는 "ADMIN"

            // 4) Spring Security 권한 객체 생성 ("ROLE_USER" 형식으로)
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
            List<SimpleGrantedAuthority> authorities = List.of(authority);

            // 5) 인증 객체 생성 및 세부 정보 설정
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 6) SecurityContext에 인증 정보 등록
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // 7) 다음 필터로 진행
        chain.doFilter(request, response);
    }
}
