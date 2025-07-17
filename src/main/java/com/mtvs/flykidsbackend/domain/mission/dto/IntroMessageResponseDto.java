package com.mtvs.flykidsbackend.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntroMessageResponseDto {

    @Schema(description = "미션 ID", example = "1")
    private Long missionId;

    @Schema(description = "미션 시작 안내 멘트", example = "오늘 너의 첫 번째 임무는 하늘에 떠 있는 신비한 동전들을 모으는 거야!")
    private String introMessage;
}
