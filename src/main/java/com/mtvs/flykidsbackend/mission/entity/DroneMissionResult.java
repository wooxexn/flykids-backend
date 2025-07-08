package com.mtvs.flykidsbackend.mission.entity;

import com.mtvs.flykidsbackend.mission.model.MissionResultStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 미션 수행 결과 엔티티
 * - 유저가 특정 미션을 완료한 결과를 기록하는 테이블
 * - 점수, 이탈 횟수, 소요 시간 등 성과 데이터를 저장한다
 * - 리더보드, 학습 이력 조회 등 기능에서 활용됨
 */
/**
 * 미션 수행 결과 엔티티
 * - 유저가 특정 미션을 완료한 결과를 기록하는 테이블
 * - 점수, 이탈 횟수, 소요 시간 등 성과 데이터를 저장한다
 * - 미션 성공 여부(success)를 포함하여 성공/실패 판단에 활용
 * - 리더보드, 학습 이력 조회 등 기능에서 활용됨
 */
@Entity
@Table(name = "drone_mission_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DroneMissionResult {

    /** 결과 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 미션 수행한 유저 ID */
    private Long userId;

    /** 사용한 드론 ID (기본 드론 1종만 사용하더라도 기록용 필드 유지) */
    private String droneId;

    /** 총 비행 시간 (단위: 초) */
    private double totalTime;

    /** 경로 이탈 횟수 */
    private int deviationCount;

    /** 장애물 충돌 횟수 */
    private int collisionCount;

    /** 최종 점수 (0~100 범위 예상) */
    private int score;

    /** 미션 성공 여부 (true: 성공, false: 실패) */
    private boolean success;

    /** 미션 완료 시각 */
    private LocalDateTime completedAt;

    /** 미션 수행 상태 (성공/실패/중단) */
    @Enumerated(EnumType.STRING)
    private MissionResultStatus status;

    /**
     * 엔티티 저장 시 완료 시간 자동 세팅
     */
    @PrePersist
    protected void onCreate() {
        this.completedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = this.success ? MissionResultStatus.SUCCESS : MissionResultStatus.FAIL;
        }
    }

    /**
     * 수행한 미션 엔티티 참조
     * - 해당 결과가 어떤 미션에 대한 것인지 나타냄
     * - 미션 ID는 mission.getId()를 통해 조회 가능
     * - LAZY 전략을 사용하여 실제 Mission 정보(title 등)는 필요 시에만 로딩됨
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    private Mission mission;
}
