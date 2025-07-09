package com.mtvs.flykidsbackend.mission.dto;

import com.mtvs.flykidsbackend.mission.model.MissionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 단일 미션 등록/수정 요청 DTO
 * - title: 미션 제목
 * - type: 미션 유형 (COIN, OBSTACLE, PHOTO)
 * - timeLimit: 제한 시간
 * - totalCoinCount: 코인 미션일 경우 요구 코인 개수
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionRequestDto {

    @Schema(description = "미션 제목", example = "드론 코인 수집 미션")
    private String title;

    @Schema(description = "미션 유형 (COIN, OBSTACLE, PHOTO)", example = "COIN")
    private MissionType type;

    @Schema(description = "제한 시간 (초 단위)", example = "180")
    private int timeLimit;

    @Schema(description = "요구 코인 개수 (COIN 미션에만 해당)", example = "10")
    private Integer totalCoinCount;
}
