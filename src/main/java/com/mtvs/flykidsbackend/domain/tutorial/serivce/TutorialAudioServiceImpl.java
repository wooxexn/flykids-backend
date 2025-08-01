package com.mtvs.flykidsbackend.domain.tutorial.serivce;

import com.mtvs.flykidsbackend.domain.tutorial.dto.TutorialAudioResponseDto;
import com.mtvs.flykidsbackend.domain.tutorial.model.TutorialFailureStep;
import com.mtvs.flykidsbackend.domain.tutorial.model.TutorialStep;
import org.springframework.stereotype.Service;

/**
 * 튜토리얼 음성 API 비즈니스 로직 구현체.
 *
 * 튜토리얼 단계 enum을 기반으로 S3에 업로드된 음성 파일 경로를 구성하여 반환한다.
 */
@Service
public class TutorialAudioServiceImpl implements TutorialAudioService {

    private static final String BASE_URL = "https://flykids-tts-files.s3.ap-northeast-2.amazonaws.com/tutorial/";

    public TutorialAudioResponseDto getAudioByStep(TutorialStep step) {
        String fileName = step.getFileName();
        String audioUrl = BASE_URL + fileName;
        return new TutorialAudioResponseDto(step.name(), audioUrl);
    }

    public TutorialAudioResponseDto getFailureAudioByStep(TutorialFailureStep failureStep) {
        String fileName = failureStep.getFileName();
        String audioUrl = BASE_URL + fileName;
        return new TutorialAudioResponseDto(failureStep.name(), audioUrl);
    }
}
