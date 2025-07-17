package com.mtvs.flykidsbackend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 성공 시 반환할 액세스 토큰과 리프레시 토큰 DTO
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Schema(description = "토큰 응답 DTO")
public class TokenResponseDto {

    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private final String accessToken;

    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private final String refreshToken;
}
