package com.mtvs.flykidsbackend.mission.entity;

import com.mtvs.flykidsbackend.mission.model.MissionResultStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 미션 단계별 수행 결과 엔티티
 * - 유저가 하나의 미션을 수행하면서 각 미션 아이템(단계)에 대해 낸 결과를 저장
 * - 재도전 시 실패한 단계만 다시 수행할 수 있도록 하기 위한 구조
 */
@Entity
@Table(name = "drone_mission_item_result")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DroneMissionItemResult {

    /** 결과 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 유저 ID (누가 수행했는지) */
    private Long userId;

    /** 미션 ID (어떤 미션인지) */
    private Long missionId;

    /** 미션 아이템 ID (미션의 어떤 단계인지) */
    private Long missionItemId;

    /** 해당 단계 수행 결과 상태 (성공/실패/미시도 등) */
    @Enumerated(EnumType.STRING)
    private MissionResultStatus status;

    /** 소요 시간 (초 단위) */
    private double duration;

    /** 경로 이탈 횟수 */
    private int deviationCount;

    /** 장애물 충돌 횟수 */
    private int collisionCount;

    /** 저장 시각 */
    private LocalDateTime createdAt;

    /** 저장 시점 자동 처리 */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
