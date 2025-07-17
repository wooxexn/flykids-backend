package com.mtvs.flykidsbackend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 시 클라이언트가 보내는 데이터를 담는 DTO 클래스
 * username(아이디)와 password(비밀번호)를 포함한다
 */
@Getter
@NoArgsConstructor
@Schema(description = "로그인 요청 DTO")
public class LoginRequestDto {

    @NotBlank(message = "아이디는 필수입니다.")
    @Schema(description = "사용자 아이디", example = "test")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Schema(description = "사용자 비밀번호", example = "test1234")
    private String password;

    public LoginRequestDto(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
