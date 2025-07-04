package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.dto.DroneMissionResultRequestDto;
import com.mtvs.flykidsbackend.mission.dto.DroneMissionResultRequestDto.MissionItemResult;
import com.mtvs.flykidsbackend.mission.entity.DroneMissionResult;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.entity.MissionItem;
import com.mtvs.flykidsbackend.mission.model.MissionType;
import com.mtvs.flykidsbackend.mission.repository.DroneMissionResultRepository;
import com.mtvs.flykidsbackend.mission.repository.MissionItemRepository;
import com.mtvs.flykidsbackend.mission.repository.MissionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.*;

/**
 * DroneMissionResultService에 대한 단위 테스트 클래스
 * - 미션 결과 저장 로직과 성공 판정 로직을 집중 테스트
 * - Mockito를 사용하여 의존성 모킹 및 동작 검증
 */
@ExtendWith(MockitoExtension.class)
class DroneMissionResultServiceTest {

    /* ────── Mock 객체 및 Service 주입 ────── */
    @Mock
    DroneMissionResultRepository resultRepo;

    @Mock
    MissionRepository missionRepo;

    @Mock
    MissionItemRepository missionItemRepo;

    @Mock
    ScoreCalculator scoreCalculator;   // 점수 계산기 모킹

    @InjectMocks
    DroneMissionResultService service;

    /* ────── 공통 픽스처 헬퍼 메서드 ────── */

    /**
     * 테스트용 Mission 엔티티 생성 헬퍼
     * @param id 미션 ID
     * @return Mission 객체
     */
    private Mission mission(Long id) {
        return Mission.builder()
                .id(id)
                .title("미션")
                .build();
    }

    /**
     * 테스트용 MissionItem 엔티티 생성 헬퍼
     * @param mId 미션 ID
     * @param type 미션 유형
     * @param totalCoin 총 코인 개수 (코인 미션일 경우)
     * @return MissionItem 객체
     */
    private MissionItem item(Long mId, MissionType type, Integer totalCoin) {
        return MissionItem.builder()
                .mission(Mission.builder().id(mId).build())
                .type(type)
                .totalCoinCount(totalCoin)
                .build();
    }

    /**
     * 테스트용 MissionItemResult DTO 생성 헬퍼
     * @param type 미션 유형
     * @param time 총 소요 시간
     * @param devCnt 이탈 횟수
     * @param colCnt 충돌 횟수
     * @param coin 획득 코인 수
     * @param photo 촬영 여부 (PHOTO 미션)
     * @return MissionItemResult DTO
     */
    private MissionItemResult itemResult(MissionType type, int time,
                                         int devCnt, int colCnt, Integer coin, Boolean photo) {
        return MissionItemResult.builder()
                .missionType(type)
                .totalTime(time)
                .deviationCount(devCnt)
                .collisionCount(colCnt)
                .collectedCoinCount(coin)
                .photoCaptured(photo)
                .build();
    }

    /* ─────────────────────────────────────────────
       1. saveComplexMissionResult() - 미션 결과 전체 저장 테스트
    ───────────────────────────────────────────── */

    @Test
    @DisplayName("모든 미션-아이템 결과가 정상 저장된다")
    void save_complexMission() {
        // given - 기본 입력 데이터 및 모킹 설정
        long userId = 100L;
        long missionId = 1L;

        // 미션 조회 시 정상 미션 객체 반환 모킹
        given(missionRepo.findById(missionId))
                .willReturn(Optional.of(mission(missionId)));

        // 미션 아이템 리스트 조회 시 코인 미션 + 장애물 미션 반환 모킹
        given(missionItemRepo.findByMissionId(missionId))
                .willReturn(List.of(
                        item(missionId, MissionType.COIN, 5),
                        item(missionId, MissionType.OBSTACLE, null)
                ));

        // 점수 계산기는 호출별로 100점, 80점 반환하도록 모킹
        given(scoreCalculator.calculateScore(
                any(MissionType.class),
                anyDouble(),
                anyInt(),
                anyInt(),
                anyInt()
        )).willReturn(100, 80);

        // 테스트용 미션 결과 DTO 생성 (코인 + 장애물)
        DroneMissionResultRequestDto dto = DroneMissionResultRequestDto.builder()
                .droneId("1")
                .itemResults(List.of(
                        itemResult(MissionType.COIN, 30, 0, 0, 5, null),
                        itemResult(MissionType.OBSTACLE, 25, 0, 2, null, null)
                ))
                .build();

        // 미션 결과 저장 시 전달 객체 그대로 반환하도록 모킹
        given(resultRepo.save(any(DroneMissionResult.class)))
                .willAnswer(inv -> inv.getArgument(0));

        // when - 서비스 호출
        List<DroneMissionResult> saved = service.saveComplexMissionResult(userId, missionId, dto);

        // then - 저장 결과 검증
        assertThat(saved).hasSize(2);                         // 2개의 결과 저장됨
        assertThat(saved.get(0).getScore()).isEqualTo(100);   // 첫번째 결과 점수 100
        assertThat(saved.get(1).getScore()).isEqualTo(80);    // 두번째 결과 점수 80

        verify(resultRepo, times(2)).save(any(DroneMissionResult.class));  // save 2회 호출 검증
    }

    /* ─────────────────────────────────────────────
       2. 미션 조회 실패 시 예외 발생 테스트
    ───────────────────────────────────────────── */

    @Test
    @DisplayName("없는 미션ID일 때 NoSuchElementException 발생")
    void save_missionNotFound() {
        // given - 미션 조회 시 빈 Optional 반환
        given(missionRepo.findById(99L)).willReturn(Optional.empty());

        DroneMissionResultRequestDto dto = DroneMissionResultRequestDto.builder()
                .droneId("1")
                .itemResults(List.of())
                .build();

        // when & then - 미션이 없으므로 예외 발생 검증
        assertThatThrownBy(() ->
                service.saveComplexMissionResult(1L, 99L, dto)
        ).isInstanceOf(NoSuchElementException.class);
    }

    /* ─────────────────────────────────────────────
       3. isMissionSuccess() - 미션 성공 판정 로직 테스트 (중첩 클래스)
    ───────────────────────────────────────────── */

    @Nested
    class MissionSuccess {

        /**
         * COIN 미션: 수집한 코인 수가 총 코인 수와 같으면 성공
         */
        @Test
        @DisplayName("COIN : 수집 개수 == 전체 코인 개수 → 성공")
        void coin_success() {
            MissionItem item = item(1L, MissionType.COIN, 3);
            MissionItemResult ir = itemResult(MissionType.COIN, 0,0,0,3,null);

            boolean success = service.isMissionSuccess(MissionType.COIN, ir, item);
            assertThat(success).isTrue();
        }

        /**
         * OBSTACLE 미션: 충돌 횟수가 3 미만이면 성공
         */
        @Test
        @DisplayName("OBSTACLE : 충돌 3회 미만 → 성공")
        void obstacle_success() {
            MissionItem item = item(1L, MissionType.OBSTACLE, null);
            MissionItemResult ir = itemResult(MissionType.OBSTACLE,0,0,2,null,null);

            assertThat(service.isMissionSuccess(MissionType.OBSTACLE, ir, item)).isTrue();
        }

        /**
         * PHOTO 미션: 촬영 여부가 true면 성공
         */
        @Test
        @DisplayName("PHOTO : photoCaptured == true → 성공")
        void photo_success() {
            MissionItem item = item(1L, MissionType.PHOTO, null);
            MissionItemResult ir = itemResult(MissionType.PHOTO,0,0,0,null,true);

            assertThat(service.isMissionSuccess(MissionType.PHOTO, ir, item)).isTrue();
        }

        /**
         * 지원하지 않는 미션 타입 입력 시 IllegalArgumentException 발생
         */
        @Test
        @DisplayName("지원하지 않는 타입이면 IllegalArgumentException 발생")
        void invalid_type() {
            MissionItem item = item(1L, MissionType.COIN, null);
            MissionItemResult ir = itemResult(MissionType.COIN,0,0,0,0,null);

            assertThatThrownBy(() ->
                    service.isMissionSuccess(null, ir, item)
            ).isInstanceOf(IllegalArgumentException.class);
        }
    }
}