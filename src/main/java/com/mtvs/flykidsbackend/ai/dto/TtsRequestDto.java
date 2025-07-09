package com.mtvs.flykidsbackend.ai.dto;

import lombok.*;

/**
 * TTS 요청 DTO
 * - 백엔드에서 AI 서버로 음성 합성 요청을 보낼 때 사용
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TtsRequestDto {

    /** 유저 ID 또는 username */
    private String userId;

    /** 미션 ID */
    private Long missionId;

    /** 미션 결과 상태 (예: "SUCCESS", "FAIL") */
    private String status;

    /** 음성으로 변환할 메시지 (최종 피드백 텍스트) */
    private String message;
}
