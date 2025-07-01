package com.mtvs.flykidsbackend.mission.entity;

import com.mtvs.flykidsbackend.mission.model.MissionType;
import jakarta.persistence.*;
import lombok.*;

/**
 * 미션 아이템 엔티티
 * - 하나의 미션 내에 포함된 개별 미션 항목을 나타낸다.
 * - 각 아이템은 미션 유형(COIN, OBSTACLE, PHOTO 등)을 가진다.
 * - Mission과 다대일(N:1) 관계로 연결된다.
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

    /** 미션 유형 (COIN / OBSTACLE / PHOTO) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionType type;

    /** 이 미션 아이템이 속한 미션 객체 (다대일 관계) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    private Mission mission;
}
