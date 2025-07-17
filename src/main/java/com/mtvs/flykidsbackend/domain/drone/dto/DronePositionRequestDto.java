package com.mtvs.flykidsbackend.domain.drone.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 드론 위치 전송 요청 DTO
 *
 * 클라이언트(유니티)에서 주기적으로 드론의 위치(x, y, z) 및 회전 각도(rotationY)를
 * 서버로 전송할 때 사용하는 데이터 전송 객체
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DronePositionRequestDto {

    @Schema(description = "드론의 x축 위치", example = "1.23")
    private double x;

    @Schema(description = "드론의 y축 위치 (고도)", example = "0.75")
    private double y;

    @Schema(description = "드론의 z축 위치", example = "5.67")
    private double z;

    @Schema(description = "드론의 y축 회전 각도 (0도 = +Z 방향)", example = "90.0")
    private double rotationY;

    @Schema(description = "현재 수행 중인 미션 ID", example = "1")
    private Long missionId;

    @Schema(description = "드론 또는 유저 식별용 ID", example = "drone-user-001")
    private String droneId;
}
