package com.mtvs.flykidsbackend.config;

import com.mtvs.flykidsbackend.config.security.CustomUserDetails;
import com.mtvs.flykidsbackend.user.entity.User;
import com.mtvs.flykidsbackend.user.repository.UserRepository;
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
    private final UserRepository userRepository;

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

            // username으로 사용자 조회 (없으면 null 반환)
            User user = userRepository.findByUsername(username)
                    .orElse(null);

            // 사용자 없거나 탈퇴 상태(INACTIVE)인 경우 인증 실패 처리
            if (user == null || user.getStatus() == User.UserStatus.INACTIVE) {
                // 한글 깨짐 방지용 인코딩 설정
                response.setContentType("text/plain;charset=UTF-8");
                response.setCharacterEncoding("UTF-8");

                // 인증 실패 상태 코드 설정
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                // 메시지 쓰기
                response.getWriter().write("탈퇴된 사용자입니다.");
                return;
            }

            String role = jwtUtil.getUserRole(token); // 예: "USER" 또는 "ADMIN"

            // 4) 권한 생성
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
            List<SimpleGrantedAuthority> authorities = List.of(authority);

            // 5) 인증 객체 생성
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(new CustomUserDetails(user), null, authorities);

            // 6) 인증 정보에 요청 정보 추가
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 7) SecurityContext에 인증 정보 등록
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // 8) 다음 필터로 진행
        chain.doFilter(request, response);
    }
}
