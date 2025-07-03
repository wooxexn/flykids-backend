package com.mtvs.flykidsbackend.drone.service;

import com.mtvs.flykidsbackend.drone.dto.RoutePointRequestDto;
import com.mtvs.flykidsbackend.drone.entity.RoutePoint;
import com.mtvs.flykidsbackend.drone.repository.RoutePointRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * RoutePointService 단위 테스트 클래스
 * - 경로 좌표 조회 및 저장 기능 테스트
 * - Mockito를 활용한 Repository Mock 처리
 */
@ExtendWith(MockitoExtension.class)
class RoutePointServiceTest {

    @Mock
    RoutePointRepository routePointRepository;

    @InjectMocks
    RoutePointService routePointService;

    /**
     * getRouteByMissionId() 메서드 정상 조회 테스트
     * - 특정 missionId에 대해 저장된 RoutePoint 리스트를 반환하는지 검증
     */
    @Test
    @DisplayName("getRouteByMissionId - 정상 조회")
    void getRouteByMissionId_success() {
        // given
        Long missionId = 1L;
        List<RoutePoint> mockPoints = List.of(
                RoutePoint.builder().missionId(1L).x(1.0).y(2.0).z(3.0).build()
        );
        given(routePointRepository.findByMissionId(missionId)).willReturn(mockPoints);

        // when
        List<RoutePoint> result = routePointService.getRouteByMissionId(missionId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMissionId()).isEqualTo(1L);
        verify(routePointRepository).findByMissionId(missionId);
    }

    /**
     * saveRoutePoints() 관련 테스트 묶음
     */
    @Nested
    class SaveRoutePointsTest {

        /**
         * saveRoutePoints() 정상 저장 테스트
         * - 유효한 RoutePointRequestDto 리스트가 저장소에 저장되는지 검증
         */
        @Test
        @DisplayName("saveRoutePoints - 정상 저장")
        void saveRoutePoints_success() {
            // given
            List<RoutePointRequestDto> dtoList = List.of(
                    RoutePointRequestDto.builder()
                            .missionId(1L)
                            .x(1.0)
                            .y(2.0)
                            .z(3.0)
                            .build()
            );

            // when
            routePointService.saveRoutePoints(dtoList);

            // then
            verify(routePointRepository).saveAll(anyList());
        }

        /**
         * saveRoutePoints() 빈 리스트 입력 시 예외 발생 테스트
         */
        @Test
        @DisplayName("saveRoutePoints - 빈 리스트 예외")
        void saveRoutePoints_emptyList() {
            // expect
            assertThatThrownBy(() -> routePointService.saveRoutePoints(List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("저장할 경로 좌표 목록이 없습니다.");
        }

        /**
         * saveRoutePoints() 미션 ID가 누락된 입력 시 예외 발생 테스트
         */
        @Test
        @DisplayName("saveRoutePoints - missionId 누락 예외")
        void saveRoutePoints_invalidMissionId() {
            // given
            List<RoutePointRequestDto> dtoList = List.of(
                    RoutePointRequestDto.builder()
                            .missionId(null) // 유효하지 않은 missionId
                            .x(1.0)
                            .y(2.0)
                            .z(3.0)
                            .build()
            );

            // expect
            assertThatThrownBy(() -> routePointService.saveRoutePoints(dtoList))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 미션 ID");
        }
    }
}
