package com.mtvs.flykidsbackend.domain.mission.dto;

import com.mtvs.flykidsbackend.domain.mission.entity.Mission;
import com.mtvs.flykidsbackend.domain.mission.model.MissionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 미션 응답 DTO
 * - 클라이언트에 반환할 미션 정보
 * - 여러 단계별 미션 아이템을 리스트로 포함
 */
@Getter
@Builder
@AllArgsConstructor
public class MissionResponseDto {

    @Schema(description = "미션 ID", example = "1")
    private Long id;

    @Schema(description = "미션 제목", example = "드론 퀘스트")
    private String title;

    @Schema(description = "제한 시간 (초 단위)", example = "180")
    private int timeLimit;

    @Schema(description = "미션 유형", example = "COIN")
    private MissionType type;

    @Schema(description = "요구 코인 개수 (코인 미션일 경우)", example = "10")
    private Integer totalCoinCount;

    @Schema(description = "미션 시작 안내 멘트", example = "오늘 너의 첫 번째 임무는 하늘에 떠 있는 신비한 동전들을 모으는 거야!")
    private String introMessage;

    public static MissionResponseDto from(Mission mission) {
        return MissionResponseDto.builder()
                .id(mission.getId())
                .title(mission.getTitle())
                .timeLimit(mission.getTimeLimit())
                .type(mission.getType())
                .totalCoinCount(mission.getTotalCoinCount())
                .introMessage(mission.getIntroMessage())
                .build();
    }
}
