package com.mtvs.flykidsbackend.domain.tutorial.dto;

/**
 * 튜토리얼 음성 응답 DTO.
 *
 * 튜토리얼 단계에 해당하는 음성 파일의 S3 URL을 클라이언트에 전달한다.
 * 요청된 {@link package com.mtvs.flykidsbackend.domain.tutorial.model.TutorialStep} 값에 따라
 * 사운드 파일 URL을 구성해 반환한다.
 *
 * @param step 튜토리얼 단계명
 * @param audioUrl S3에 업로드된 튜토리얼 음성 파일의 전체 URL
 */
public record TutorialAudioResponseDto(
        String step,
        String audioUrl
) {
}
