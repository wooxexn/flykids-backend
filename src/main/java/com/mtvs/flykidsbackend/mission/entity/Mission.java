package com.mtvs.flykidsbackend.mission.entity;

import com.mtvs.flykidsbackend.mission.model.MissionType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 미션 엔티티
 * - 복합 미션 단위로 관리
 * - 여러 MissionItem을 포함하는 구조로 변경
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

    /**
     * 미션 제한 시간 (초 단위)
     * - 전체 미션의 수행 가능 최대 시간
     * - 모든 단계별 미션(Item)에 공통 적용 가능
     * - 게임 클라이언트 및 백엔드에서 제한시간 검증에 사용
     */
    @Column(nullable = false)
    private int timeLimit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionType type;

    @Column
    private Integer totalCoinCount;

    /**
     * 미션 순서
     * - 낮은 숫자일수록 먼저 수행해야 함
     * - 예: orderIndex = 1 → 첫 번째 미션
     */
    @Column(name = "order_index")
    private Integer orderIndex;

    /** 미션 잠금 여부 (true = 잠김, false = 열림) */
    @Column(nullable = false)
    private boolean locked = true;

    /** 잠금 해제 처리 */
    public void unlock() {
        this.locked = false;
    }
}
