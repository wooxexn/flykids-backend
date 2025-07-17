package com.mtvs.flykidsbackend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 비밀번호 수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비밀번호 수정 요청 DTO")
public class UpdatePasswordRequestDto {

    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    @Schema(description = "현재 비밀번호", example = "test1234")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Schema(description = "새 비밀번호", example = "test4567")
    private String newPassword;
}
