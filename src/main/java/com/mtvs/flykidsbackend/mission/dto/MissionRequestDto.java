package com.mtvs.flykidsbackend.mission.dto;

import com.mtvs.flykidsbackend.mission.model.MissionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 미션 등록/수정 요청 DTO
 * - title: 미션 제목 (전체 미션의 이름)
 * - timeLimit: 제한 시간 (초 단위)
 * - items: 단계별 미션 아이템 리스트
 */
@Getter
@Setter
@NoArgsConstructor
public class MissionRequestDto {

    @Schema(description = "미션 제목", example = "드론 퀘스트")
    private String title;

    @Schema(description = "제한 시간 (초 단위)", example = "180")
    private int timeLimit;

    @Schema(description = "단계별 미션 아이템 리스트")
    private List<MissionItemDto> items;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MissionItemDto {

        @Schema(description = "미션 아이템 제목", example = "코인 미션")
        private String title;

        @Schema(description = "미션 유형 (COIN, OBSTACLE, PHOTO)", example = "COIN")
        private MissionType type;

        @Schema(description = "미션 아이템 제한 시간 (초 단위)", example = "60")
        private int timeLimit;

        @Schema(description = "코인 미션에서 요구하는 총 코인 개수", example = "10")
        private Integer totalCoinCount;
    }
}
