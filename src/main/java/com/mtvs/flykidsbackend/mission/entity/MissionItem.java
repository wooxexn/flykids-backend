package com.mtvs.flykidsbackend.mission.entity;

import com.mtvs.flykidsbackend.mission.model.MissionType;
import jakarta.persistence.*;
import lombok.*;

/**
 * 미션 아이템 엔티티
 * - 복합 미션 내 각 단계별 미션을 나타냄
 * - 미션(Quest) 하나에 여러 MissionItem이 포함될 수 있음
 */
@Entity
@Table(name = "mission_items")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MissionItem {

    /** 미션 아이템 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 소속 미션 (다대일 관계) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    /** 미션 아이템 제목 */
    @Column(nullable = false)
    private String title;

    /** 제한 시간 (초 단위) */
    @Column(nullable = false)
    private int timeLimit;

    /** 미션 유형 (COIN / OBSTACLE / PHOTO) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionType type;

    /** 코인 미션에서 요구하는 총 코인 개수 */
    @Column(nullable = true)
    private Integer totalCoinCount;
}
