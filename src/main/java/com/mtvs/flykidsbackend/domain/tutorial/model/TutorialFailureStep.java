package com.mtvs.flykidsbackend.domain.tutorial.model;

/**
 * 튜토리얼 실패 상황에 대한 음성 파일 Enum 정의.
 *
 * <AI가 생성한 튜토리얼 실패 음성 파일(tutorial_failure_1.wav ~ tutorial_failure_3.wav)과 1:1로 매핑된다.
 */
public enum TutorialFailureStep {
    FAILURE_1,
    FAILURE_2,
    FAILURE_3;

    /**
     * 튜토리얼 실패 음성 파일명 반환 (예: tutorial_failure_1.wav)
     */
    public String getFileName() {
        int number = this.ordinal() + 1;
        return "tutorial_failure_" + number + ".wav";
    }
}