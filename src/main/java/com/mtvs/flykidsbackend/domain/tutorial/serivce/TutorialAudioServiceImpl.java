package com.mtvs.flykidsbackend.domain.tutorial.serivce;

import com.mtvs.flykidsbackend.domain.tutorial.dto.TutorialAudioResponseDto;
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

    /**
     * 튜토리얼 단계에 해당하는 음성 파일의 S3 URL을 반환한다.
     *
     * @param step 튜토리얼 단계 enum
     * @return 해당 음성의 S3 URL이 포함된 응답 DTO
     */
    @Override
    public TutorialAudioResponseDto getAudioByStep(TutorialStep step) {

        int stepNumber = step.ordinal() + 1;
        String fileName = stepNumber + ".wav";
        String audioUrl = BASE_URL + fileName;

        return new TutorialAudioResponseDto(step.name(), audioUrl);
    }
}
