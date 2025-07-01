package com.mtvs.flykidsbackend.drone.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 드론 위치 처리 응답 DTO
 *
 * status: 응답 상태 코드 (예: OK, OUT_OF_BOUNDS, ALTITUDE_ERROR, COLLISION)
 * message: 사용자 또는 시스템용 응답 메시지
 */
@Getter
@Setter
@AllArgsConstructor
@Schema(description = "드론 응답 DTO")
public class DroneResponse {
    @Schema(description = "응답 상태", example = "OK / COLLISION / OUT_OF_BOUNDS")
    private String status;  // 예: OK, OUT_OF_BOUNDS, ALTITUDE_ERROR, COLLISION

    @Schema(description = "응답 메시지", example = "드론 위치가 정상적으로 저장되었습니다.")
    private String message; // 사용자에게 표시할 메시지
}
