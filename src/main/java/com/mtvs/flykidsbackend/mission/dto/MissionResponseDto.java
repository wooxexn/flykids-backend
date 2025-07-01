package com.mtvs.flykidsbackend.mission.dto;

import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.model.MissionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 미션 응답 DTO
 * - 클라이언트에 반환할 미션 정보
 */
@Getter
@Builder
@AllArgsConstructor
public class MissionResponseDto {

    private Long id;
    private String title;
    private int timeLimit;
    private MissionType type;

    /**
     * Mission 엔티티를 MissionResponseDto로 변환
     */
    public static MissionResponseDto from(Mission mission) {
        return MissionResponseDto.builder()
                .id(mission.getId())
                .title(mission.getTitle())
                .timeLimit(mission.getTimeLimit())
                .type(mission.getType())
                .build();
    }
}
