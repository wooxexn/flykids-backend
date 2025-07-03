package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.entity.UserMissionProgress;
import com.mtvs.flykidsbackend.mission.repository.UserMissionProgressRepository;
import com.mtvs.flykidsbackend.user.entity.User;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.entity.MissionItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * UserMissionProgressServiceImpl에 대한 단위 테스트 클래스
 * - Repository 목(mock) 객체를 주입하여 Service 로직만 검증
 * - JUnit5, Mockito 확장 사용
 */
@ExtendWith(MockitoExtension.class)
class UserMissionProgressServiceImplTest {

    @Mock
    private UserMissionProgressRepository progressRepository;

    @InjectMocks
    private UserMissionProgressServiceImpl progressService;

    private User user;
    private Mission mission;
    private MissionItem missionItem;

    /**
     * 테스트 공통 객체 초기화
     */
    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testuser").build();
        mission = Mission.builder().id(10L).title("미션1").build();
        missionItem = MissionItem.builder().id(100L).title("미션 아이템1").build();
    }

    /**
     * 특정 유저, 미션, 미션 아이템별 진행 정보 조회 테스트
     */
    @Test
    void getProgress_기존진행정보조회() {
        // given: 조회 시 반환할 목 객체 준비
        UserMissionProgress progress = UserMissionProgress.builder()
                .user(user)
                .mission(mission)
                .missionItem(missionItem)
                .status("IN_PROGRESS")
                .build();

        when(progressRepository.findByUserAndMissionAndMissionItem(user, mission, missionItem))
                .thenReturn(Optional.of(progress));

        // when: 서비스 호출
        Optional<UserMissionProgress> result = progressService.getProgress(user, mission, missionItem);

        // then: 결과 검증 및 메서드 호출 검증
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo("IN_PROGRESS");
        verify(progressRepository).findByUserAndMissionAndMissionItem(user, mission, missionItem);
    }

    /**
     * 특정 유저와 미션에 대한 모든 단계별 진행 정보 리스트 조회 테스트
     */
    @Test
    void getProgressList_모든단계조회() {
        // given: 리스트 반환 목 객체 준비
        List<UserMissionProgress> list = List.of(
                UserMissionProgress.builder().status("DONE").build(),
                UserMissionProgress.builder().status("IN_PROGRESS").build()
        );
        when(progressRepository.findByUserAndMission(user, mission)).thenReturn(list);

        // when
        List<UserMissionProgress> result = progressService.getProgressList(user, mission);

        // then
        assertThat(result).hasSize(2);
        verify(progressRepository).findByUserAndMission(user, mission);
    }

    /**
     * 진행 정보 저장 또는 수정 메서드 테스트
     */
    @Test
    void saveOrUpdateProgress_저장호출() {
        // given: 저장할 객체 및 저장 후 반환 객체 설정
        UserMissionProgress progress = UserMissionProgress.builder()
                .user(user).mission(mission).missionItem(missionItem).status("DONE").build();

        when(progressRepository.save(progress)).thenReturn(progress);

        // when
        UserMissionProgress saved = progressService.saveOrUpdateProgress(progress);

        // then
        assertThat(saved).isEqualTo(progress);
        verify(progressRepository).save(progress);
    }

    /**
     * 특정 상태별 미션 진행 정보 리스트 조회 테스트
     */
    @Test
    void getProgressByStatus_상태별조회() {
        // given
        String status = "DONE";
        List<UserMissionProgress> list = List.of(
                UserMissionProgress.builder().status(status).build()
        );
        when(progressRepository.findByUserAndMissionAndStatus(user, mission, status)).thenReturn(list);

        // when
        List<UserMissionProgress> result = progressService.getProgressByStatus(user, mission, status);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(status);
        verify(progressRepository).findByUserAndMissionAndStatus(user, mission, status);
    }

    /**
     * updateStatus 메서드 테스트 - 기존 진행 정보가 있을 때
     * - 기존 객체 상태 변경 후 저장하는지 검증
     */
    @Test
    void updateStatus_기존진행정보존재시_상태변경후저장() {
        // given
        UserMissionProgress existingProgress = UserMissionProgress.builder()
                .user(user)
                .mission(mission)
                .missionItem(missionItem)
                .status("IN_PROGRESS")
                .build();

        when(progressRepository.findByUserAndMissionAndMissionItem(user, mission, missionItem))
                .thenReturn(Optional.of(existingProgress));
        when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        progressService.updateStatus(user, mission, missionItem, "DONE");

        // then
        assertThat(existingProgress.getStatus()).isEqualTo("DONE");
        verify(progressRepository).save(existingProgress);
    }

    /**
     * updateStatus 메서드 테스트 - 기존 진행 정보 없을 때
     * - 새 객체 생성 후 저장하는지 검증
     */
    @Test
    void updateStatus_진행정보없을때_새객체생성후저장() {
        // given
        when(progressRepository.findByUserAndMissionAndMissionItem(user, mission, missionItem))
                .thenReturn(Optional.empty());
        when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        progressService.updateStatus(user, mission, missionItem, "DONE");

        // then
        ArgumentCaptor<UserMissionProgress> captor = ArgumentCaptor.forClass(UserMissionProgress.class);
        verify(progressRepository).save(captor.capture());

        UserMissionProgress saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getMission()).isEqualTo(mission);
        assertThat(saved.getMissionItem()).isEqualTo(missionItem);
        assertThat(saved.getStatus()).isEqualTo("DONE");
    }
}
