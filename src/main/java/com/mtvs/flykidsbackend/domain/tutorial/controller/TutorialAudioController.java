package com.mtvs.flykidsbackend.domain.tutorial.controller;

import com.mtvs.flykidsbackend.domain.tutorial.dto.TutorialAudioResponseDto;
import com.mtvs.flykidsbackend.domain.tutorial.model.TutorialStep;
import com.mtvs.flykidsbackend.domain.tutorial.serivce.TutorialAudioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 튜토리얼 음성 API 컨트롤러.
 *
 * 튜토리얼 단계에 해당하는 음성 파일 URL을 반환하는 엔드포인트를 제공한다.
 */
@Tag(name = "Tutorial Audio", description = "튜토리얼 음성 재생을 위한 API")
@RestController
@RequestMapping("/api/tutorials/audio")
@RequiredArgsConstructor
public class TutorialAudioController {

    private final TutorialAudioService tutorialAudioService;

    /**
     * 튜토리얼 단계에 해당하는 음성 파일 URL을 반환한다.
     *
     * @param step 튜토리얼 단계 enum (STEP_1 ~ STEP_28)
     * @return 해당 단계의 음성 URL 응답
     */
    @Operation(
            summary = "튜토리얼 음성 URL 조회",
            description = "튜토리얼 단계(STEP_1 ~ STEP_28)에 해당하는 음성 파일의 S3 URL을 반환합니다.\n"
                    + "각 단계는 1.wav ~ 28.wav에 매핑되어 있으며, 클라이언트는 해당 URL을 통해 음성을 재생할 수 있습니다."
    )
    @GetMapping("/{step}")
    public ResponseEntity<TutorialAudioResponseDto> getAudio(@PathVariable("step") TutorialStep step) {
        return ResponseEntity.ok(tutorialAudioService.getAudioByStep(step));
    }
}
