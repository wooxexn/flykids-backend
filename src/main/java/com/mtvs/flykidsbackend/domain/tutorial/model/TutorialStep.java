package com.mtvs.flykidsbackend.domain.tutorial.model;

/**
 * 튜토리얼 단계 Enum 정의.
 *
 * <AI가 생성한 튜토리얼 음성 파일(tutorial_1.wav ~ tutorial_20.wav)과 1:1로 매핑되는 enum 값이다.
 * 각 enum은 해당 번호의 음성 파일(예: STEP_3 → tutorial_3.wav)과 자동 연결된다.
 */
public enum TutorialStep {
    STEP_1,
    STEP_2,
    STEP_3,
    STEP_4,
    STEP_5,
    STEP_6,
    STEP_7,
    STEP_8,
    STEP_9,
    STEP_10,
    STEP_11,
    STEP_12,
    STEP_13,
    STEP_14,
    STEP_15,
    STEP_16,
    STEP_17,
    STEP_18,
    STEP_19,
    STEP_20;

    /**
     * 튜토리얼 음성 파일명 반환 (예: tutorial_1.wav)
     */
    public String getFileName() {
        int number = this.ordinal() + 1;
        return "tutorial_" + number + ".wav";
    }
}