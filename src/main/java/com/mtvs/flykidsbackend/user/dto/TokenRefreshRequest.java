package com.mtvs.flykidsbackend.user.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 클라이언트가 액세스 토큰이 만료되었을 때
 * 리프레시 토큰을 이용해 새 액세스 토큰을 요청할 때 사용하는 DTO 클래스
 */
@Getter
@Setter
public class TokenRefreshRequest {

    /**
     * 클라이언트가 보유한 리프레시 토큰
     * 이 토큰을 검증하여 새 액세스 토큰을 발급할 수 있다
     */
    private String refreshToken;
}