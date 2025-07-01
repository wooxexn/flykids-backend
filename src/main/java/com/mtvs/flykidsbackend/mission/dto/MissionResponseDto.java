package com.mtvs.flykidsbackend.mission.dto;

import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.model.MissionType;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "미션 ID", example = "1")
    private Long id;

    @Schema(description = "미션 제목", example = "코인 미션")
    private String title;

    @Schema(description = "제한 시간 (초 단위)", example = "60")
    private int timeLimit;

    @Schema(description = "미션 유형 (COIN, OBSTACLE, PHOTO)", example = "COIN")
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

