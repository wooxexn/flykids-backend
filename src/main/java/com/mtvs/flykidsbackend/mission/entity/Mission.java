package com.mtvs.flykidsbackend.mission.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

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

    /**
     * 미션에 포함된 여러 미션 아이템 리스트
     * - MissionItem 엔티티의 mission 필드와 양방향 매핑
     * - CascadeType.ALL: 미션 저장/삭제 시 연관된 아이템들도 함께 처리
     * - orphanRemoval = true: 미션에서 아이템이 제거되면 DB에서도 삭제
     */
    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MissionItem> items;

    public void setItems(List<MissionItem> items) {
        this.items = items;
        if (items != null) {
            items.forEach(item -> item.setMission(this));
        }
    }
}
