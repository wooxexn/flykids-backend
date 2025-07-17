package com.mtvs.flykidsbackend.domain.mission.service;

import com.mtvs.flykidsbackend.domain.mission.dto.DroneMissionResultRequestDto;
import com.mtvs.flykidsbackend.domain.mission.dto.MissionCompleteResponseDto;
import com.mtvs.flykidsbackend.domain.mission.entity.DroneMissionResult;
import com.mtvs.flykidsbackend.domain.mission.entity.Mission;
import com.mtvs.flykidsbackend.domain.mission.model.MissionResultStatus;
import com.mtvs.flykidsbackend.domain.mission.model.MissionType;
import com.mtvs.flykidsbackend.domain.mission.repository.DroneMissionResultRepository;
import com.mtvs.flykidsbackend.domain.mission.repository.MissionRepository;
import com.mtvs.flykidsbackend.domain.user.entity.Role;
import com.mtvs.flykidsbackend.domain.user.entity.User;
import com.mtvs.flykidsbackend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MissionService 통합 테스트
 *
 * 미션 완료 처리 메서드의 실제 동작을 검증한다.
 * 사용자가 드론 미션을 성공적으로 완료했을 때 다음 항목을 확인한다:
 * - 점수 계산
 * - 미션 성공 여부 판단
 * - 결과 DB 저장
 * - 피드백 메시지 및 음성 URL 반환
 */
@SpringBootTest
public class MissionServiceIntegrationTest {

    @Autowired private MissionService missionService;
    @Autowired private MissionRepository missionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private DroneMissionResultRepository resultRepository;

    @Test
    @DisplayName("드론 미션 완료 처리 - 성공 시 점수 계산, 상태 저장 및 응답 메시지 반환")
    void completeMission_success() {
        // given: 테스트용 사용자 및 미션 생성
        String uniqueUsername = "test_" + UUID.randomUUID(); // 유니크한 ID 생성

        User user = userRepository.save(User.builder()
                .username(uniqueUsername)
                .password("test")
                .nickname("전우선")
                .status(User.UserStatus.ACTIVE)
                .role(Role.USER)
                .build());

        Mission mission = missionRepository.save(Mission.builder()
                .title("동전 미션")
                .type(MissionType.COIN)
                .timeLimit(60)
                .totalCoinCount(5)
                .orderIndex(1)
                .introMessage("모든 동전을 모아보세요!")
                .build());

        DroneMissionResultRequestDto.MissionItemResult item = DroneMissionResultRequestDto.MissionItemResult.builder()
                .totalTime(35.0)
                .deviationCount(0)
                .collisionCount(0)
                .collectedCoinCount(5) // → 성공 조건 (모든 동전 수집)
                .build();

        DroneMissionResultRequestDto requestDto = DroneMissionResultRequestDto.builder()
                .droneId("drone-001")
                .itemResult(item)
                .build();

        // when: 미션 완료 처리 호출
        MissionCompleteResponseDto response = missionService.completeMission(user.getId(), mission.getId(), requestDto);

        // then: 응답 내용 검증
        assertThat(response.isSuccess())
                .as("미션이 성공했는지 여부")
                .isTrue();

        assertThat(response.getScore())
                .as("점수는 0 이상이어야 함")
                .isGreaterThan(0);

        assertThat(response.getMessage())
                .as("성공 메시지는 안내 문구를 포함해야 함")
                .contains("완벽하게");

        assertThat(response.getAudioUrl())
                .as("성공 음성 URL이 포함되어야 함")
                .contains("mission_success");

        // then: 실제 DB 저장된 결과 확인
        DroneMissionResult result = resultRepository
                .findByUserIdAndMissionId(user.getId(), mission.getId())
                .stream()
                .findFirst()
                .orElse(null);

        assertThat(result)
                .as("미션 결과는 DB에 저장되어야 함")
                .isNotNull();

        assertThat(result.getStatus())
                .as("결과 상태는 SUCCESS여야 함")
                .isEqualTo(MissionResultStatus.SUCCESS);

        assertThat(result.getScore())
                .as("응답과 저장된 점수는 일치해야 함")
                .isEqualTo(response.getScore());
    }
}
