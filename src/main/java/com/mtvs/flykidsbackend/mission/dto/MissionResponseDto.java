package com.mtvs.flykidsbackend.mission.dto;

import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.entity.MissionItem;
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
 * - 복합 미션일 경우 포함된 미션 아이템 리스트도 같이 전달
 */
@Getter
@Builder
@AllArgsConstructor
public class MissionResponseDto {

    @Schema(description = "미션 ID", example = "1")
    private Long id;

    @Schema(description = "미션 제목", example = "복합 미션")
    private String title;

    @Schema(description = "제한 시간 (초 단위)", example = "60")
    private int timeLimit;

    @Schema(description = "미션 아이템 리스트")
    private List<MissionItemResponseDto> items;

    /**
     * 미션 아이템 응답 DTO
     * - 각 미션 아이템의 유형 정보를 담는다
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class MissionItemResponseDto {
        @Schema(description = "미션 아이템 유형", example = "COIN")
        private MissionType type;

        /**
         * MissionItem 엔티티를 MissionItemResponseDto로 변환
         */
        public static MissionItemResponseDto from(MissionItem item) {
            return MissionItemResponseDto.builder()
                    .type(item.getType())
                    .build();
        }
    }

    /**
     * Mission 엔티티를 MissionResponseDto로 변환
     * - 복합 미션일 경우 포함된 MissionItem도 변환하여 리스트로 포함
     */
    public static MissionResponseDto from(Mission mission) {
        List<MissionItemResponseDto> itemDtos = mission.getItems().stream()
                .map(MissionItemResponseDto::from)
                .collect(Collectors.toList());

        return MissionResponseDto.builder()
                .id(mission.getId())
                .title(mission.getTitle())
                .timeLimit(mission.getTimeLimit())
                .items(itemDtos)
                .build();
    }
}