package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.dto.DroneMissionResultRequestDto;
import com.mtvs.flykidsbackend.mission.entity.DroneMissionResult;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.model.MissionType;
import com.mtvs.flykidsbackend.mission.repository.DroneMissionResultRepository;
import com.mtvs.flykidsbackend.mission.repository.MissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.*;

/**
 * DroneMissionResultService 단위 테스트 (단일 미션 기준)
 */
@ExtendWith(MockitoExtension.class)
class DroneMissionResultServiceTest {

    @Mock
    DroneMissionResultRepository resultRepo;

    @Mock
    MissionRepository missionRepo;

    @Mock
    ScoreCalculator scoreCalculator;

    @InjectMocks
    DroneMissionResultService service;

    private Mission mission;

    /**
     * 테스트용 Mission 엔티티 생성 헬퍼
     */
    private Mission createMission(Long id, MissionType type, Integer totalCoinCount) {
        return Mission.builder()
                .id(id)
                .title("단일 미션 테스트")
                .type(type)
                .totalCoinCount(totalCoinCount)
                .build();
    }

    @BeforeEach
    void setUp() {
        // 기본 테스트용 미션 생성
        mission = createMission(1L, MissionType.COIN, 5);
    }

    /**
     * 단일 미션 결과 저장 테스트
     */
    @Test
    @DisplayName("단일 미션 결과 정상 저장")
    void saveMissionResult_success() {
        long userId = 1L;
        long missionId = 1L;

        // 미션 조회 모킹
        given(missionRepo.findById(missionId)).willReturn(Optional.of(mission));

        // 점수 계산기 모킹 (100점 리턴)
        given(scoreCalculator.calculateScore(eq(MissionType.COIN), any(DroneMissionResultRequestDto.class))).willReturn(100);

        // 저장 시 전달된 객체 그대로 리턴
        given(resultRepo.save(any(DroneMissionResult.class))).willAnswer(inv -> inv.getArgument(0));

        // 단일 아이템 결과 DTO 준비
        DroneMissionResultRequestDto.MissionItemResult itemResult =
                DroneMissionResultRequestDto.MissionItemResult.builder()
                        .missionType(MissionType.COIN)
                        .totalTime(60)
                        .deviationCount(0)
                        .collisionCount(0)
                        .collectedCoinCount(5)
                        .photoCaptured(null)
                        .build();

        DroneMissionResultRequestDto dto = DroneMissionResultRequestDto.builder()
                .droneId("drone1")
                .itemResult(itemResult)
                .build();

        // 서비스 호출
        DroneMissionResult savedResult = service.saveMissionResult(userId, missionId, dto);

        // 결과 검증
        assertThat(savedResult).isNotNull();
        assertThat(savedResult.getUserId()).isEqualTo(userId);
        assertThat(savedResult.getMission()).isEqualTo(mission);
        assertThat(savedResult.getScore()).isEqualTo(100);

        // 저장 메서드 호출 횟수 검증
        verify(resultRepo, times(1)).save(any(DroneMissionResult.class));
    }

    /**
     * 미션 조회 실패 시 예외 발생 테스트
     */
    @Test
    @DisplayName("미션 ID 없으면 NoSuchElementException 발생")
    void saveMissionResult_missionNotFound() {
        long missionId = 999L;

        // 미션 없음 모킹
        given(missionRepo.findById(missionId)).willReturn(Optional.empty());

        DroneMissionResultRequestDto dto = DroneMissionResultRequestDto.builder()
                .droneId("drone1")
                .itemResult(null)
                .build();

        assertThatThrownBy(() -> service.saveMissionResult(1L, missionId, dto))
                .isInstanceOf(NoSuchElementException.class);
    }

    /**
     * 미션 성공 판정 테스트 (단일 미션)
     */
    @Test
    @DisplayName("미션 성공 판정: COIN 미션")
    void isMissionSuccess_coin() {
        Mission mission = createMission(1L, MissionType.COIN, 5);

        DroneMissionResultRequestDto.MissionItemResult itemResult =
                DroneMissionResultRequestDto.MissionItemResult.builder()
                        .missionType(MissionType.COIN)
                        .collectedCoinCount(5)
                        .build();

        DroneMissionResultRequestDto dto = DroneMissionResultRequestDto.builder()
                .itemResult(itemResult)
                .build();

        boolean success = service.isMissionSuccess(MissionType.COIN, itemResult, mission);

        assertThat(success).isTrue();
    }

    /**
     * 지원하지 않는 타입일 경우 예외 테스트
     */
    @Test
    @DisplayName("미션 타입이 null이면 예외 발생")
    void isMissionSuccess_invalidType() {
        DroneMissionResultRequestDto.MissionItemResult itemResult = DroneMissionResultRequestDto.MissionItemResult.builder()
                .missionType(MissionType.COIN)
                .collectedCoinCount(5)
                .build();

        DroneMissionResultRequestDto dto = DroneMissionResultRequestDto.builder()
                .itemResult(itemResult)
                .build();

        assertThatThrownBy(() -> service.isMissionSuccess(null, itemResult, mission))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
