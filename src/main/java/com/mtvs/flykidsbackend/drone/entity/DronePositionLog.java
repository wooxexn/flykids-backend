package com.mtvs.flykidsbackend.drone.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 *  드론 위치 로그 엔티티
 *
 * 유니티에서 전송된 드론의 위치 및 방향 데이터를 기록하는 테이블
 * 미션 수행 중 실시간 이동 경로 분석, 경로 이탈 판별 등에 활용됨
 *
 * Fields:
 * - id: 기본 키 (자동 생성)
 * - droneId: 드론 또는 유저 식별자
 * - missionId: 수행 중인 미션 ID
 * - x, y, z: 드론의 3D 위치 좌표
 * - rotationY: 드론의 방향(회전 각도), Y축 기준
 * - loggedAt: 로그가 기록된 시각 (기본값: 현재 시각)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class DronePositionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String droneId;

    private Long missionId;

    private double x;
    private double y;
    private double z;

    private double rotationY;

    private LocalDateTime loggedAt = LocalDateTime.now();
}
