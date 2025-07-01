package com.mtvs.flykidsbackend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 닉네임 수정 요청 DTO
 */
@Getter
@Setter
@Schema(description = "닉네임 수정 요청 DTO")
public class UpdateNicknameRequestDto {

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Schema(description = "새 닉네임", example = "테스터2")
    private String nickname;
}
