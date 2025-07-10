package com.mtvs.flykidsbackend.drone.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 드론 위치 처리 응답 DTO
 *
 * status: 응답 상태 코드 (예: OK, OUT_OF_BOUNDS, ALTITUDE_ERROR, COLLISION)
 * message: 사용자 또는 시스템용 응답 메시지
 * audioUrl: (선택) 상황별 음성 피드백 mp3 URL
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "드론 위치 처리 응답")
public class DroneResponse {

    @Schema(
            description = "응답 상태 코드",
            example = "OK",
            allowableValues = {"OK", "COLLISION", "OUT_OF_BOUNDS", "ALTITUDE_ERROR", "ERROR"}
    )
    private String status;

    @Schema(
            description = "응답 메시지 (예: 사용자에게 전달될 메시지)",
            example = "드론 위치가 정상적으로 저장되었습니다."
    )
    private String message;

    @Schema(
            description = "음성 피드백 mp3 파일의 S3 URL",
            example = "https://flykids-audio-files.s3.ap-northeast-2.amazonaws.com/feedback_collision.mp3",
            nullable = true
    )
    private String audioUrl;
}
