package com.mtvs.flykidsbackend.drone.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 드론 위치 전송 요청 DTO
 *
 * 클라이언트(유니티)에서 주기적으로 드론의 위치(x, y, z) 및 회전 각도(rotationY)를
 * 서버로 전송할 때 사용하는 데이터 전송 객체
 *
 * 사용처: POST /api/drone/position-log
 * 용도: 드론의 실시간 위치/방향 정보를 서버에 기록하고,
 *       향후 미션 수행 여부 판단 등에 활용함.
 *
 * Fields:
 *  - x: 드론의 x축 위치
 *  - y: 드론의 y축 위치
 *  - z: 드론의 z축 위치
 *  - rotationY: 드론의 y축 회전 각도 (0도 = +Z방향)
 *  - missionId: 현재 수행 중인 미션 식별자
 *  - droneId: 드론 또는 유저 식별용 ID
 */
@Getter
@Setter
public class DronePositionRequestDto {
    private double x;
    private double y;
    private double z;
    private double rotationY;
    private Long missionId;
    private String droneId;
}
