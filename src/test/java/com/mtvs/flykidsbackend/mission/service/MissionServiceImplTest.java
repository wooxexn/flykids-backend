package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.dto.DroneMissionResultRequestDto;
import com.mtvs.flykidsbackend.mission.dto.MissionRequestDto;
import com.mtvs.flykidsbackend.mission.entity.DroneMissionResult;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.model.MissionType;
import com.mtvs.flykidsbackend.mission.repository.DroneMissionResultRepository;
import com.mtvs.flykidsbackend.mission.repository.MissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MissionServiceImplTest {

    @Mock private MissionRepository missionRepository;
    @Mock private DroneMissionResultRepository resultRepository;
    @Mock private ScoreCalculator scoreCalculator;

    @InjectMocks private MissionServiceImpl service;

    private DroneMissionResultRequestDto requestDto;
    private Mission mission;
    private DroneMissionResultRequestDto.MissionItemResult itemResult;

    @BeforeEach
    void setUp() {
        mission = Mission.builder()
                .id(1L)
                .title("단일 미션 테스트")
                .type(MissionType.COIN)
                .totalCoinCount(5)
                .timeLimit(60)
                .build();

        itemResult = DroneMissionResultRequestDto.MissionItemResult.builder()
                .missionType(MissionType.COIN)
                .totalTime(30)
                .deviationCount(0)
                .collisionCount(0)
                .collectedCoinCount(5)
                .build();

        // 단일 구조에 맞게 리스트 대신 단일 필드로 설정
        requestDto = DroneMissionResultRequestDto.builder()
                .droneId("drone-1")
                .itemResult(itemResult)  // 리스트가 아닌 단일 객체로 설정
                .build();
    }



    @Test
    void createMission_정상저장() {
        when(missionRepository.save(any(Mission.class))).thenReturn(mission);

        var dto = MissionRequestDto.builder()
                .title("단일 미션 테스트")
                .build();

        assertThat(dto.getTitle()).isEqualTo("단일 미션 테스트");
        verify(missionRepository).save(any(Mission.class));
    }

    @Test
    void completeMission_성공() {
        when(missionRepository.findById(1L)).thenReturn(Optional.of(mission));
        when(scoreCalculator.calculateScore(any(), any())).thenReturn(100);
        when(scoreCalculator.isMissionSuccess(any(), any(), any())).thenReturn(true);
        when(resultRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.completeMission(1L, 1L, requestDto);

        assertThat(result.getScore()).isEqualTo(100);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).isNotEmpty();

        verify(resultRepository).save(any(DroneMissionResult.class));
    }

    @Test
    void getMission_정상조회() {
        when(missionRepository.findById(1L)).thenReturn(Optional.of(mission));

        var dto = service.getMission(1L);

        assertThat(dto.getTitle()).isEqualTo("단일 미션 테스트");
    }
}
