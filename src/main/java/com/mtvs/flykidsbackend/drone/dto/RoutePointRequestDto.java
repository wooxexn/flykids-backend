package com.mtvs.flykidsbackend.drone.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 기준 경로 등록 요청 DTO
 * 미션 ID와 개별 좌표(XYZ)를 담는다.
 */
@Getter
@Setter
public class RoutePointRequestDto {
    private Long missionId;
    private double x;
    private double y;
    private double z;
}
