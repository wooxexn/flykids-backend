package com.mtvs.flykidsbackend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 내 정보 조회 응답 DTO
 */
@Getter
@AllArgsConstructor
@Schema(description = "내 정보 조회 응답 DTO")
public class UserInfoResponseDto {

    /** 로그인 ID (중복 불가) */
    @Schema(description = "로그인 아이디", example = "test")
    private String username;

    /** 사용자 닉네임 (화면 표시용) */
    @Schema(description = "사용자 닉네임", example = "테스터")
    private String nickname;

    /** 권한 (예: USER, ADMIN) */
    @Schema(description = "사용자 권한", example = "USER")
    private String role;
}