package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.dto.*;
import com.mtvs.flykidsbackend.mission.entity.*;
import com.mtvs.flykidsbackend.mission.model.MissionType;
import com.mtvs.flykidsbackend.mission.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * MissionServiceImpl에 대한 단위 테스트 클래스
 * - Mockito를 사용하여 Repository 및 외부 의존성을 Mock 처리
 * - 주요 서비스 메서드의 정상 동작을 검증한다
 */
@ExtendWith(MockitoExtension.class)
class MissionServiceImplTest {

    // Mock 객체 선언: 실제 DB 호출 대신 테스트용 Mock 객체를 사용
    @Mock private MissionRepository missionRepository;
    @Mock private MissionItemRepository missionItemRepository;
    @Mock private DroneMissionResultRepository resultRepository;
    @Mock private DroneMissionResultService resultService;
    @Mock private ScoreCalculator scoreCalculator;

    // 테스트 대상 서비스에 Mock 객체 주입
    @InjectMocks
    private MissionServiceImpl service;

    // 테스트 시 공통으로 사용할 미션 요청 DTO
    private MissionRequestDto requestDto;

    /**
     * 테스트 실행 전 공통 데이터 셋업
     * - 미션 요청에 사용할 미션 아이템과 미션 제목 설정
     */
    @BeforeEach
    void setUp() {
        MissionRequestDto.MissionItemDto item = MissionRequestDto.MissionItemDto.builder()
                .title("장애물 통과")
                .timeLimit(60)
                .type(MissionType.OBSTACLE)
                .build();

        requestDto = MissionRequestDto.builder()
                .title("테스트 미션")
                .items(List.of(item))
                .build();
    }

    /**
     * createMission() 메서드 정상 동작 테스트
     * - 미션 저장 시 리포지토리가 올바른 객체를 반환하는지 모킹
     * - 미션 아이템 저장 시 리포지토리 동작 모킹
     * - 반환된 DTO의 제목 검증
     * - Repository save 호출 여부 검증
     */
    @Test
    void createMission_정상동작() {
        Mission mission = Mission.builder().id(1L).title("테스트 미션").build();
        when(missionRepository.save(any())).thenReturn(mission);

        MissionItem item = MissionItem.builder()
                .id(1L)
                .title("장애물 통과")
                .type(MissionType.OBSTACLE)
                .mission(mission)
                .build();
        when(missionItemRepository.save(any())).thenReturn(item);

        MissionResponseDto response = service.createMission(requestDto);

        assertThat(response.getTitle()).isEqualTo("테스트 미션");
        verify(missionRepository).save(any(Mission.class));
        verify(missionItemRepository).save(any(MissionItem.class));
    }

    /**
     * updateMission() 메서드 정상 동작 테스트
     * - 기존 미션 아이템 삭제 및 새로운 아이템 저장 로직 검증
     * - findById, save 모킹 처리
     * - 미션 아이템 저장 시 Mission 필드 null 체크 후 셋팅
     * - 반환된 DTO의 제목 검증
     * - 미션 아이템 삭제 메서드 호출 검증
     */
    @Test
    void updateMission_기존아이템삭제_새로저장() {
        Mission mission = Mission.builder()
                .id(1L)
                .title("기존 미션")
                .missionItems(new ArrayList<>())
                .build();

        when(missionRepository.findById(1L)).thenReturn(Optional.of(mission));
        when(missionItemRepository.save(any())).thenAnswer(invocation -> {
            MissionItem mi = invocation.getArgument(0);
            if (mi.getMission() == null) {
                mi.setMission(mission);
            }
            return mi;
        });

        // save 호출 시 인자로 받은 객체를 그대로 반환하도록 설정
        when(missionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MissionResponseDto result = service.updateMission(1L, requestDto);

        assertThat(result.getTitle()).isEqualTo("테스트 미션");
        verify(missionItemRepository).deleteAllByMissionId(1L);
    }

    /**
     * deleteMission() 메서드 정상 동작 테스트
     * - 존재하는 미션에 대해 삭제 수행 여부 검증
     * - existsById, deleteById 호출 모킹 및 검증
     */
    @Test
    void deleteMission_존재시_삭제수행() {
        when(missionRepository.existsById(1L)).thenReturn(true);
        service.deleteMission(1L);
        verify(missionRepository).deleteById(1L);
    }

    /**
     * getMission() 메서드 정상 조회 테스트
     * - findById 호출 시 미션 객체 반환 모킹
     * - 반환된 DTO의 제목 검증
     */
    @Test
    void getMission_정상조회() {
        Mission mission = Mission.builder()
                .id(1L)
                .title("조회용 미션")
                .missionItems(new ArrayList<>())
                .build();

        when(missionRepository.findById(1L)).thenReturn(Optional.of(mission));

        MissionResponseDto result = service.getMission(1L);
        assertThat(result.getTitle()).isEqualTo("조회용 미션");
    }

    /**
     * completeMission() 메서드 정상 동작 테스트
     * - 미션 아이템 결과 기반 점수 계산 및 성공 여부 로직 모킹
     * - 결과 저장 시 save 호출 모킹
     * - 반환된 응답 DTO 점수, 성공 여부, 메시지 내용 검증
     */
    @Test
    void completeMission_정상동작() {
        MissionItem missionItem = MissionItem.builder()
                .id(1L)
                .type(MissionType.COIN)
                .totalCoinCount(5)
                .build();

        Mission mission = Mission.builder()
                .id(1L)
                .title("복합 미션")
                .missionItems(List.of(missionItem))
                .build();

        DroneMissionResultRequestDto.MissionItemResult itemResult =
                DroneMissionResultRequestDto.MissionItemResult.builder()
                        .missionType(MissionType.COIN)
                        .totalTime(20)
                        .deviationCount(0)
                        .collisionCount(0)
                        .collectedCoinCount(5)
                        .build();

        DroneMissionResultRequestDto dto = DroneMissionResultRequestDto.builder()
                .droneId("1")
                .itemResults(List.of(itemResult))
                .build();

        when(missionRepository.findById(1L)).thenReturn(Optional.of(mission));
        when(scoreCalculator.calculateScore(any(), anyDouble(), anyInt(), anyInt(), anyInt())).thenReturn(100);
        when(scoreCalculator.isMissionSuccess(any(), any(), any())).thenReturn(true);
        when(resultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MissionCompleteResponseDto result = service.completeMission(1L, 1L, dto);

        assertThat(result.getScore()).isEqualTo(100);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("성공");
    }
}
