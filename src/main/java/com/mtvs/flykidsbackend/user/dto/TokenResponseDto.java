package com.mtvs.flykidsbackend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 로그인 성공 시 반환할 액세스 토큰과 리프레시 토큰 DTO
 */
@Getter
@AllArgsConstructor
public class TokenResponseDto {
    private final String accessToken;
    private final String refreshToken;
}
