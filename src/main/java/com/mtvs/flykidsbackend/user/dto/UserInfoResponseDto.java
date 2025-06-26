package com.mtvs.flykidsbackend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 내 정보 조회 응답 DTO
 */
@Getter
@AllArgsConstructor
public class UserInfoResponseDto {

    /** 로그인 ID (중복 불가) */
    private String username;

    /** 사용자 닉네임 (화면 표시용) */
    private String nickname;

    /** 권한 (예: USER, ADMIN) */
    private String role;
}