package com.mtvs.flykidsbackend.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 닉네임 수정 요청 DTO
 */
@Getter
@Setter
public class UpdateNicknameRequestDto {

    @NotBlank(message = "닉네임을 입력해주세요.")
    private String nickname;
}