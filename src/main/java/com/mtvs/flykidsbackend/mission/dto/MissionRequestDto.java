package com.mtvs.flykidsbackend.mission.dto;

import com.mtvs.flykidsbackend.mission.model.MissionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private String title;
    private int timeLimit;
    private MissionType type;
}
