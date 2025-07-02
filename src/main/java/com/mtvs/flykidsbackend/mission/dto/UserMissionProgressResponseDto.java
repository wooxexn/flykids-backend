package com.mtvs.flykidsbackend.mission.dto;

import com.mtvs.flykidsbackend.mission.entity.UserMissionProgress;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 유저 미션 진행 상태 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class UserMissionProgressResponseDto {

    @Schema(description = "진행 ID", example = "1")
    private Long id;

    @Schema(description = "미션 ID", example = "10")
    private Long missionId;

    @Schema(description = "미션 아이템 ID", example = "100")
    private Long missionItemId;

    @Schema(description = "진행 상태", example = "COMPLETED")
    private String status;

    @Schema(description = "마지막 상태 변경 시간")
    private LocalDateTime updatedAt;

    public static UserMissionProgressResponseDto from(UserMissionProgress entity) {
        return UserMissionProgressResponseDto.builder()
                .id(entity.getId())
                .missionId(entity.getMission().getId())
                .missionItemId(entity.getMissionItem().getId())
                .status(entity.getStatus())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
