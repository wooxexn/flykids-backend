package com.mtvs.flykidsbackend.user.controller;

import com.mtvs.flykidsbackend.config.JwtUtil;
import com.mtvs.flykidsbackend.user.dto.TokenRefreshRequest;
import com.mtvs.flykidsbackend.user.dto.TokenResponseDto;
import com.mtvs.flykidsbackend.user.entity.User;
import com.mtvs.flykidsbackend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }

        // 토큰 타입 확인 (리프레시 토큰인지)
        if (!"refresh".equals(jwtUtil.getTokenType(refreshToken))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not a refresh token");
        }

        String username = jwtUtil.getUsername(refreshToken);

        // 유저 존재 여부 확인 등 추가 검증 가능

        // 새로운 액세스 토큰 생성
        User user = userService.findByUsername(username);
        String newAccessToken = jwtUtil.createAccessToken(user.getUsername(), user.getRole().name());

        return ResponseEntity.ok(new TokenResponseDto(newAccessToken, refreshToken));
    }
}