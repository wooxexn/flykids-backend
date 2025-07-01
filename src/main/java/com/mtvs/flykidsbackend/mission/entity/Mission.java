package com.mtvs.flykidsbackend.mission.entity;

import com.mtvs.flykidsbackend.mission.model.MissionType;
import jakarta.persistence.*;
import lombok.*;

/**
 * 미션 엔티티
 * - 단일 미션 타입으로 관리 (COIN / OBSTACLE / PHOTO)
 */
@Entity
@Table(name = "missions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Mission {

    /** 미션 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 미션 이름 */
    @Column(nullable = false)
    private String title;

    /** 제한 시간 (초 단위) */
    @Column(nullable = false)
    private int timeLimit;

    /** 미션 유형 (COIN / OBSTACLE / PHOTO) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionType type;

}
