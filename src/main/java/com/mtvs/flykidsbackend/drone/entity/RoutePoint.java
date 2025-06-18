package com.mtvs.flykidsbackend.drone.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 기준 경로 포인트 엔티티
 *
 * 각 미션에 따라 기준 경로를 구성하는 좌표 점이다.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long missionId;

    private double x;
    private double y;
    private double z;
}
