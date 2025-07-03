package com.mtvs.flykidsbackend.mission.dto;

import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.model.MissionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

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

    @Schema(description = "단계별 미션 아이템 리스트")
    private List<MissionItemResponseDto> items;

    /**
     * Mission 엔티티를 MissionResponseDto로 변환하는 메서드
     * - 복합 미션 구조에서 Mission은 여러 MissionItem을 포함함
     * - Mission의 ID와 제목을 DTO에 담고,
     *   MissionItem 리스트를 MissionItemResponseDto 리스트로 변환하여 포함시킴
     *
     * @param mission 변환할 Mission 엔티티
     * @return MissionResponseDto 객체
     */
    public static MissionResponseDto from(Mission mission) {
        return MissionResponseDto.builder()
                .id(mission.getId())
                .title(mission.getTitle())
                .items(
                        mission.getMissionItems() == null
                                ? List.of()
                                : mission.getMissionItems().stream()
                                .map(MissionItemResponseDto::from)
                                .collect(Collectors.toList())
                )
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class MissionItemResponseDto {

        @Schema(description = "미션 아이템 ID", example = "10")
        private Long id;

        @Schema(description = "미션 아이템 제목", example = "코인 미션")
        private String title;

        @Schema(description = "미션 유형", example = "COIN")
        private MissionType type;

        @Schema(description = "제한 시간 (초 단위)", example = "60")
        private int timeLimit;

        @Schema(description = "요구 코인 개수 (코인 미션일 경우)", example = "10")
        private Integer totalCoinCount;

        public static MissionItemResponseDto from(com.mtvs.flykidsbackend.mission.entity.MissionItem item) {
            return MissionItemResponseDto.builder()
                    .id(item.getId())
                    .title(item.getTitle())
                    .type(item.getType())
                    .timeLimit(item.getTimeLimit())
                    .totalCoinCount(item.getTotalCoinCount())
                    .build();
        }
    }
}
