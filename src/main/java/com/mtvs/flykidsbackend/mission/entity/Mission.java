package com.mtvs.flykidsbackend.mission.entity;

import com.mtvs.flykidsbackend.mission.model.MissionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "missions")
@Getter
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
