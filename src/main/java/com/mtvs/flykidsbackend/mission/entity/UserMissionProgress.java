package com.mtvs.flykidsbackend.mission.entity;

import com.mtvs.flykidsbackend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 유저별 미션 진행 상태 기록 엔티티
 * - 한 유저가 특정 미션(퀘스트)의 각 미션 아이템 단계를 수행하며 상태 관리
 */
@Entity
@Table(name = "user_mission_progress")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserMissionProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저 (다대일)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 미션 (퀘스트) (다대일)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    // 미션 아이템 (단계) (다대일)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_item_id", nullable = false)
    private MissionItem missionItem;

    // 진행 상태 (예: PENDING, IN_PROGRESS, COMPLETED 등)
    @Column(nullable = false, length = 20)
    private String status;

    // 마지막 상태 변경 시간
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        this.updatedAt = LocalDateTime.now();
    }
}
