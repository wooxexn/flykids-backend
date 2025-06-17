package com.mtvs.flykidsbackend.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 시 클라이언트가 보내는 데이터를 담는 DTO 클래스
 * username(아이디)와 password(비밀번호)를 포함한다
 */
@Getter
@NoArgsConstructor
public class LoginRequestDto {

    /**
     * 사용자 아이디 (null 또는 공백 불가)
     * 로그인 시 입력한 아이디
     */
    @NotBlank(message = "아이디는 필수입니다.")
    private String username;

    /**
     * 사용자 비밀번호 (null 또는 공백 불가)
     * 로그인 시 입력한 비밀번호
     */
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}