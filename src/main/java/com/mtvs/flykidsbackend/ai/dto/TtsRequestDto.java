package com.mtvs.flykidsbackend.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * TTS 요청용 DTO
 * - 유니티에서 보낸 텍스트를 TTS 서버에 전달하기 위한 요청 데이터
 */
@Data
public class TtsRequestDto {

    @Schema(
            description = "음성으로 변환할 텍스트",
            example = "드론을 앞으로 이동시켜보세요!"
    )
    private String text;
}
