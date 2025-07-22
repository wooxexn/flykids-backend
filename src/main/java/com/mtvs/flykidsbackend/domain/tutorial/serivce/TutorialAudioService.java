package com.mtvs.flykidsbackend.domain.tutorial.serivce;

import com.mtvs.flykidsbackend.domain.tutorial.dto.TutorialAudioResponseDto;
import com.mtvs.flykidsbackend.domain.tutorial.model.TutorialStep;

/**
 * 튜토리얼 음성 API 비즈니스 로직 인터페이스.
 *
 * 튜토리얼 단계에 따라 대응하는 음성 파일 URL을 반환한다.
 */
public interface TutorialAudioService {

    /**
     * 튜토리얼 단계에 해당하는 음성 파일 URL을 반환한다.
     *
     * @param step 튜토리얼 단계 enum
     * @return 응답 DTO (단계명과 S3 URL 포함)
     */
    TutorialAudioResponseDto getAudioByStep(TutorialStep step);
}
