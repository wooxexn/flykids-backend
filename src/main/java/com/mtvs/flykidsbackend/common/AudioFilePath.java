package com.mtvs.flykidsbackend.common;

/**
 * S3에 업로드된 고정 음성(mp3,wav) 파일들의 URL 상수 클래스
 *
 * <적용 대상>
 * - 미션 시작 안내 음성
 * - 미션 성공/실패 피드백 음성
 * - 실시간 경고용 TTS 음성 (경로 이탈, 고도 이탈, 충돌 등)
 *
 * <주의사항>
 * - 파일명 변경 시 클라이언트와 API 응답 모두 영향 받음
 * - BASE_URL은 S3 버킷의 공개 접근 URL이어야 함
 */
public class AudioFilePath {

    /** S3 버킷의 기본 경로 (ap-northeast-2: 서울 리전) */
    private static final String BASE_URL = "https://flykids-tts-files.s3.ap-northeast-2.amazonaws.com/";

    /** 미션별 시작 안내 멘트 */
    public static final String MISSION1_INTRO = BASE_URL + "mission1_intro.wav";
    public static final String MISSION2_INTRO = BASE_URL + "mission2_intro.wav";
    public static final String MISSION3_INTRO = BASE_URL + "mission3_intro.wav";

    /** 미션 완료/실패 피드백 멘트 */
    public static final String MISSION_SUCCESS     = BASE_URL + "mission_success.wav";         // 성공 공통
    public static final String MISSION_FAIL_COMMON = BASE_URL + "mission_fail_common.wav";     // 실패 공통
    public static final String MISSION1_FAIL       = BASE_URL + "mission1_fail.wav";
    public static final String MISSION2_FAIL       = BASE_URL + "mission2_fail.wav";
    public static final String MISSION3_FAIL       = BASE_URL + "mission3_fail.wav";

    /** 실시간 경고용 멘트 */
    public static final String FEEDBACK_DEVIATION       = BASE_URL + "feedback_deviation.wav";       // 경로 이탈
    public static final String FEEDBACK_ALTITUDE_LOW    = BASE_URL + "feedback_altitude_low.wav";    // 고도 낮음
    public static final String FEEDBACK_ALTITUDE_HIGH   = BASE_URL + "feedback_altitude_high.wav";   // 고도 높음
    public static final String FEEDBACK_COLLISION       = BASE_URL + "feedback_collision.wav";       // 충돌 감지
}