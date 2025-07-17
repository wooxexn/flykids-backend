package com.mtvs.flykidsbackend.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 미션 중단 요청 DTO
 * - 클라이언트가 미션 중단 시 전달하는 정보
 * - 드론 ID만 포함됨
 */
@Getter
@Setter
@NoArgsConstructor
public class DroneAbortRequestDto {

    @Schema(description = "드론 ID", example = "basic_1", required = true)
    private String droneId;
}
