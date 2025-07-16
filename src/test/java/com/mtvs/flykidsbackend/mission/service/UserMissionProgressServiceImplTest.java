package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.entity.UserMissionProgress;
import com.mtvs.flykidsbackend.mission.repository.UserMissionProgressRepository;
import com.mtvs.flykidsbackend.user.entity.User;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.user.model.UserMissionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * UserMissionProgressServiceImpl 단일 미션 단위 구조에 맞춘 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class UserMissionProgressServiceImplTest {

    @Mock
    private UserMissionProgressRepository progressRepository;

    @InjectMocks
    private UserMissionProgressServiceImpl progressService;

    private User user;
    private Mission mission;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testuser").build();
        mission = Mission.builder().id(10L).title("미션1").build();
    }

    /**
     * 유저+미션 단일 진행 정보 조회 테스트
     */
    @Test
    void getProgress_기존진행정보조회() {
        UserMissionProgress progress = UserMissionProgress.builder()
                .user(user)
                .mission(mission)
                .status(UserMissionStatus.IN_PROGRESS)
                .build();

        when(progressRepository.findByUserAndMission(user, mission))
                .thenReturn(Optional.of(progress));

        Optional<UserMissionProgress> result = progressService.getProgress(user, mission);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(UserMissionStatus.IN_PROGRESS);
        verify(progressRepository).findByUserAndMission(user, mission);
    }

    /**
     * 유저 미션 진행 정보 저장/수정 테스트
     */
    @Test
    void saveOrUpdateProgress_저장호출() {
        UserMissionProgress progress = UserMissionProgress.builder()
                .user(user)
                .mission(mission)
                .status(UserMissionStatus.SUCCESS)
                .build();

        when(progressRepository.save(progress)).thenReturn(progress);

        UserMissionProgress saved = progressService.saveOrUpdateProgress(progress);

        assertThat(saved).isEqualTo(progress);
        verify(progressRepository).save(progress);
    }

    /**
     * 상태별 미션 진행 정보 리스트 조회 테스트
     */
    @Test
    void getProgressByStatus_상태별조회() {
        UserMissionStatus status = UserMissionStatus.SUCCESS;
        List<UserMissionProgress> list = List.of(
                UserMissionProgress.builder().status(status).build()
        );

        when(progressRepository.findByUserAndMissionAndStatus(user, mission, status)).thenReturn(list);

        List<UserMissionProgress> result = progressService.getProgressByStatus(user, status);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(status);
        verify(progressRepository).findByUserAndMissionAndStatus(user, mission, status);
    }

    /**
     * updateStatus() - 기존 진행 정보가 있을 때 상태 변경 및 저장 테스트
     */
    @Test
    void updateStatus_기존진행정보존재시_상태변경후저장() {
        UserMissionProgress existingProgress = UserMissionProgress.builder()
                .user(user)
                .mission(mission)
                .status(UserMissionStatus.IN_PROGRESS)
                .build();

        when(progressRepository.findByUserAndMission(user, mission))
                .thenReturn(Optional.of(existingProgress));
        when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        progressService.updateStatus(user, mission, UserMissionStatus.SUCCESS);

        assertThat(existingProgress.getStatus()).isEqualTo(UserMissionStatus.SUCCESS);
        verify(progressRepository).save(existingProgress);
    }

    /**
     * updateStatus() - 기존 진행 정보 없을 때 새 객체 생성 후 저장 테스트
     */
    @Test
    void updateStatus_진행정보없을때_새객체생성후저장() {
        when(progressRepository.findByUserAndMission(user, mission))
                .thenReturn(Optional.empty());
        when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        progressService.updateStatus(user, mission, UserMissionStatus.SUCCESS);  // Enum

        ArgumentCaptor<UserMissionProgress> captor = ArgumentCaptor.forClass(UserMissionProgress.class);
        verify(progressRepository).save(captor.capture());

        UserMissionProgress saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getMission()).isEqualTo(mission);
        assertThat(saved.getStatus()).isEqualTo(UserMissionStatus.SUCCESS);
    }
}
