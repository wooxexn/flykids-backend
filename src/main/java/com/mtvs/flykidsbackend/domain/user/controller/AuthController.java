package com.mtvs.flykidsbackend.domain.user.controller;

import com.mtvs.flykidsbackend.config.JwtUtil;
import com.mtvs.flykidsbackend.domain.user.dto.TokenRefreshRequest;
import com.mtvs.flykidsbackend.domain.user.dto.TokenResponseDto;
import com.mtvs.flykidsbackend.domain.user.entity.User;
import com.mtvs.flykidsbackend.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "JWT 토큰 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    /**
     * 리프레시 토큰으로 액세스 토큰 재발급
     * POST /api/auth/refresh-token
     */
    @Operation(
            summary = "액세스 토큰 재발급",
            description = "리프레시 토큰을 이용해 새로운 액세스 토큰을 발급합니다. 토큰의 유효성 및 타입을 검증한 후 정상적인 요청이면 새 액세스 토큰을 반환합니다."
    )
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(@RequestBody TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        // 리프레시 토큰 유효성 검사
        if (!jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 리프레시 토큰입니다.");
        }

        // 토큰 타입 확인 (리프레시 토큰인지)
        if (!"refresh".equals(jwtUtil.getTokenType(refreshToken))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("리프레시 토큰이 아닙니다.");
        }

        String username = jwtUtil.getUsername(refreshToken);

        // 유저 존재 여부 확인 (null 체크)
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 새로운 액세스 토큰 생성
        String newAccessToken = jwtUtil.createAccessToken(user.getUsername(), user.getRole().name());

        return ResponseEntity.ok(new TokenResponseDto(newAccessToken, refreshToken, user.getNickname()));
    }
}