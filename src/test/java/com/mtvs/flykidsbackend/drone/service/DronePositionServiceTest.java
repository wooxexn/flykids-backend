package com.mtvs.flykidsbackend.drone.service;

import com.mtvs.flykidsbackend.drone.dto.DronePositionRequestDto;
import com.mtvs.flykidsbackend.drone.dto.DroneResponse;
import com.mtvs.flykidsbackend.drone.entity.DronePositionLog;
import com.mtvs.flykidsbackend.drone.entity.RoutePoint;
import com.mtvs.flykidsbackend.drone.repository.DronePositionLogRepository;
import com.mtvs.flykidsbackend.drone.repository.RouteDeviationLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * DronePositionService 단위 테스트
 * - 드론 위치 저장과 상태 판단 로직 검증
 * - 경로 이탈, 고도 이탈, 충돌 판단 등 주요 시나리오 테스트
 */
@ExtendWith(MockitoExtension.class)
class DronePositionServiceTest {

    @Mock
    DronePositionLogRepository dronePosRepo;

    @Mock
    RoutePointService routePointService;

    @Mock
    RouteDeviationLogRepository deviationRepo;

    @InjectMocks
    DronePositionService service;

    /**
     * 테스트용 DronePositionRequestDto 생성 헬퍼 메서드
     *
     * @param x 드론 위치 X 좌표
     * @param y 드론 위치 Y 좌표 (고도)
     * @param z 드론 위치 Z 좌표
     * @param rotY 드론 회전 각도 Y축
     * @param missionId 미션 ID
     * @return 테스트용 요청 DTO
     */
    private DronePositionRequestDto req(Double x, Double y, Double z, Double rotY, Long missionId) {
        return DronePositionRequestDto.builder()
                .droneId("D1")
                .missionId(missionId)
                .x(x).y(y).z(z)
                .rotationY(rotY)
                .build();
    }

    /**
     * 테스트용 RoutePoint 엔티티 생성 헬퍼 메서드
     *
     * @param x 경로 X 좌표
     * @param y 경로 Y 좌표
     * @param z 경로 Z 좌표
     * @return RoutePoint 엔티티
     */
    private RoutePoint route(double x, double y, double z) {
        return RoutePoint.builder().x(x).y(y).z(z).build();
    }

    /**
     * 1) 정상 위치 저장 테스트
     * - 드론이 기준 경로 내에 있고 고도도 허용 범위 내일 때
     * - 충돌 의심 로그 저장 안 함
     */
    @Test
    @DisplayName("OK - 기준 경로 안, 고도 허용범위, 충돌 Trace X")
    void save_ok() {
        // given: 기준 경로와 직전 로그 상태 설정
        DronePositionRequestDto req = req(0.0, 1.5, 0.0, 0.0, 1L);
        given(routePointService.getRouteByMissionId(1L))
                .willReturn(List.of(route(0, 1.5, 0))); // 기준 경로 위치

        given(dronePosRepo.findTopByDroneIdAndLoggedAtBeforeOrderByLoggedAtDesc(any(), any()))
                .willReturn(Optional.empty()); // 직전 로그 없음

        // when: 위치 저장 수행
        DroneResponse res = service.savePosition(req);

        // then: 상태 OK, 위치 저장, 이탈 로그 저장 안함 검증
        assertThat(res.getStatus()).isEqualTo("OK");
        verify(dronePosRepo).save(any(DronePositionLog.class));
        verify(deviationRepo, never()).save(any());
    }

    /**
     * 2) 경로 이탈 시 테스트
     * - 기준 경로에서 2.5m 이상 벗어난 경우
     * - 이탈 로그 저장 여부 검증
     */
    @Test
    @DisplayName("OUT_OF_BOUNDS - 기준 경로 2.5m 초과")
    void save_outOfBounds() {
        // given: 경로 원점, 드론 위치 3m 떨어짐 (이탈)
        DronePositionRequestDto req = req(3.0, 1.5, 0.0, 0.0, 1L);
        given(routePointService.getRouteByMissionId(1L))
                .willReturn(List.of(route(0, 1.5, 0)));

        // when
        DroneResponse res = service.savePosition(req);

        // then: 상태 OUT_OF_BOUNDS, 이탈 로그 저장 확인
        assertThat(res.getStatus()).isEqualTo("OUT_OF_BOUNDS");
        verify(deviationRepo).save(any());
    }

    /**
     * 3) 고도 이탈 관련 테스트 묶음
     */
    @Nested
    class AltitudeError {

        /**
         * 고도 너무 낮을 때 상태 ALTITUDE_ERROR 반환 테스트
         */
        @Test
        @DisplayName("ALTITUDE_ERROR - 고도 너무 낮음")
        void low() {
            DronePositionRequestDto req = req(0.0, 0.1, 0.0, 0.0, 1L);
            given(routePointService.getRouteByMissionId(1L))
                    .willReturn(List.of(route(0, 0.1, 0)));

            DroneResponse res = service.savePosition(req);
            assertThat(res.getStatus()).isEqualTo("ALTITUDE_ERROR");
        }

        /**
         * 고도 너무 높을 때 상태 ALTITUDE_ERROR 반환 테스트
         */
        @Test
        @DisplayName("ALTITUDE_ERROR - 고도 너무 높음")
        void high() {
            DronePositionRequestDto req = req(0.0, 4.0, 0.0, 0.0, 1L);
            given(routePointService.getRouteByMissionId(1L))
                    .willReturn(List.of(route(0, 4, 0)));

            DroneResponse res = service.savePosition(req);
            assertThat(res.getStatus()).isEqualTo("ALTITUDE_ERROR");
        }
    }

    /**
     * 4) 충돌 감지 테스트
     * - 직전 로그 대비 회전 각도 차이가 45도 초과 시 충돌로 간주
     */
    @Test
    @DisplayName("COLLISION - 이전 로그와 회전각 변화 > 45도")
    void collision_detect() {
        // 신규 위치 요청
        DronePositionRequestDto req = req(0.0, 1.0, 0.0, 90.0, 1L);

        // 직전 로그 설정 (회전각 10도)
        DronePositionLog last = DronePositionLog.builder()
                .droneId("D1").missionId(1L).x(0).y(1).z(0).rotationY(10).build();
        given(dronePosRepo.findTopByDroneIdAndLoggedAtBeforeOrderByLoggedAtDesc(any(), any()))
                .willReturn(Optional.of(last));

        // 기준 경로 위치
        given(routePointService.getRouteByMissionId(1L))
                .willReturn(List.of(route(0, 1, 0)));

        // 위치 저장 실행 및 상태 검증
        DroneResponse res = service.savePosition(req);

        assertThat(res.getStatus()).isEqualTo("COLLISION");
    }

    /**
     * 5) 유효하지 않은 입력 처리 테스트
     * - missionId가 null일 경우 ERROR 상태와 관련 메시지 반환 검증
     */
    @Test
    @DisplayName("ERROR - missionId null")
    void invalidInput() {
        DronePositionRequestDto bad = DronePositionRequestDto.builder()
                .droneId("D1") // missionId 누락
                .x(0).y(0).z(0).rotationY(0)
                .build();

        DroneResponse res = service.savePosition(bad);

        assertThat(res.getStatus()).isEqualTo("ERROR");
        assertThat(res.getMessage()).contains("유효하지 않은");
    }
}
