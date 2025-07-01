package com.mtvs.flykidsbackend.mission.dto;

import com.mtvs.flykidsbackend.mission.model.MissionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 미션 등록/수정 요청 DTO
 * - title: 미션 제목
 * - timeLimit: 제한 시간 (초 단위)
 * - type: 미션 유형 (COIN / OBSTACLE / PHOTO)
 */
@Getter
@Setter
@NoArgsConstructor
public class MissionRequestDto {

    @Schema(description = "미션 제목", example = "복합 미션")
    private String title;

    @Schema(description = "제한 시간 (초 단위)", example = "60")
    private int timeLimit;

    @Schema(description = "미션 아이템 리스트")
    private List<MissionItemRequestDto> items;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MissionItemRequestDto {
        @Schema(description = "미션 아이템 유형", example = "COIN")
        private MissionType type;
    }
}
