package com.mtvs.flykidsbackend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO
 * - 로그인 아이디, 비밀번호, 닉네임 포함
 */
@Getter
@NoArgsConstructor
@Schema(description = "회원가입 요청 DTO")
public class SignupRequestDto {

    @NotBlank(message = "아이디는 필수입니다.")
    @Schema(description = "사용자 아이디", example = "test")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Schema(description = "사용자 비밀번호", example = "test1234")
    private String password;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Schema(description = "사용자 닉네임", example = "테스터")
    private String nickname;

    public SignupRequestDto(String username, String password, String nickname) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
    }
}
