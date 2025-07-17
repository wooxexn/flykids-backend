package com.mtvs.flykidsbackend.domain.drone.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 드론 경로 이탈 로그 엔티티
 *
 * 드론이 기준 경로를 벗어났을 때 저장되는 기록이다.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteDeviationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long missionId;

    private String droneId;

    private double x;
    private double y;
    private double z;

    private double rotationY;

    private LocalDateTime timestamp; // 발생 시간
}
