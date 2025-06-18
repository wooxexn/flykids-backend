package com.mtvs.flykidsbackend.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
public class SecurityConfig {

    /**
     * 비밀번호 암호화 빈 등록
     * BCryptPasswordEncoder를 사용해 비밀번호를 안전하게 해싱한다.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * HTTP 보안 설정 메서드
     * - CORS 설정 적용 (withDefaults 사용)
     * - CSRF 비활성화 (JWT 토큰 기반 인증이므로)
     * - 세션 상태를 Stateless로 설정 (서버 세션 사용 안 함)
     * - 회원가입, 로그인 API는 모두에게 허용
     * - 그 외 요청은 인증 필요
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())  // CorsConfig 클래스에서 정의한 CORS 정책 적용
                .csrf(csrf -> csrf.disable())  // CSRF 보호 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // 세션 사용 안 함
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/users/signup",
                                "/api/users/login",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/api/drone/position-log",
                                "/api/route/points" // POST + GET 모두 허용
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
