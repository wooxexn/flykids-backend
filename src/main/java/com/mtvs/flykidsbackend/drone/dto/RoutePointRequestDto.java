package com.mtvs.flykidsbackend.drone.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 기준 경로 등록 요청 DTO
 * 미션 ID와 개별 좌표(XYZ)를 담는다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "기준 경로 등록 요청 DTO")
public class RoutePointRequestDto {

    @Schema(description = "해당 기준 경로가 속한 미션 ID", example = "1")
    private Long missionId;

    @Schema(description = "X 좌표", example = "12.34")
    private double x;

    @Schema(description = "Y 좌표", example = "1.5")
    private double y;

    @Schema(description = "Z 좌표", example = "7.89")
    private double z;
}
