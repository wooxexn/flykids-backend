package com.mtvs.flykidsbackend.config;

import com.mtvs.flykidsbackend.config.security.CustomUserDetails;
import com.mtvs.flykidsbackend.domain.user.entity.User;
import com.mtvs.flykidsbackend.domain.user.repository.UserRepository;
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
        String uri = request.getRequestURI();

        //  permitAll 경로는 필터 통과시킴
        if (uri.startsWith("/api/tutorials/audio")
                || uri.equals("/api/users/signup")
                || uri.equals("/api/users/login")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/swagger-resources")
                || uri.startsWith("/api/route/points")
                || uri.equals("/error")
                || (uri.matches("/api/missions/.*/intro") && "GET".equals(request.getMethod()))) {
            chain.doFilter(request, response); // 필터 무시
            return;
        }

        // 기존 토큰 인증 로직 유지
        String token = jwtUtil.resolveToken(request);

        if (token != null && jwtUtil.validateToken(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String username = jwtUtil.getUsername(token);
            User user = userRepository.findByUsername(username).orElse(null);

            if (user == null || user.getStatus() == User.UserStatus.INACTIVE) {
                response.setContentType("text/plain;charset=UTF-8");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("탈퇴된 사용자입니다.");
                return;
            }

            String role = jwtUtil.getUserRole(token);
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
            List<SimpleGrantedAuthority> authorities = List.of(authority);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(new CustomUserDetails(user), null, authorities);
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }

}
