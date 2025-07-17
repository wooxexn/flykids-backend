package com.mtvs.flykidsbackend.domain.drone.service;

import com.mtvs.flykidsbackend.domain.drone.dto.RoutePointRequestDto;
import com.mtvs.flykidsbackend.domain.drone.entity.RoutePoint;
import com.mtvs.flykidsbackend.domain.drone.repository.RoutePointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 기준 경로 처리 서비스
 *
 * 기준 경로 포인트의 저장 및 조회 기능을 제공한다.
 * 미션 ID에 따라 경로를 조회하거나,
 * 클라이언트에서 전달받은 좌표 리스트를 일괄 저장할 수 있다.
 */
@Service
@RequiredArgsConstructor
public class RoutePointService {

    private final RoutePointRepository routePointRepository;

    /**
     * 특정 미션 ID에 해당하는 기준 경로 좌표 목록을 조회한다.
     *
     * @param missionId 조회할 미션 ID
     * @return 기준 경로 좌표 리스트
     */
    public List<RoutePoint> getRouteByMissionId(Long missionId) {
        return routePointRepository.findByMissionId(missionId);
    }

    /**
     * 기준 경로 좌표를 일괄 저장한다.
     * 클라이언트에서 전달받은 좌표 리스트를 엔티티로 변환 후 DB에 저장한다.
     *
     * @param pointList 저장할 기준 좌표 DTO 리스트
     */
    public void saveRoutePoints(List<RoutePointRequestDto> pointList) {
        if (pointList == null || pointList.isEmpty()) {
            throw new IllegalArgumentException("저장할 경로 좌표 목록이 없습니다.");
        }

        List<RoutePoint> routePoints = pointList.stream()
                .map(dto -> {
                    if (dto.getMissionId() == null || dto.getMissionId() <= 0) {
                        throw new IllegalArgumentException("유효하지 않은 미션 ID가 포함되어 있습니다.");
                    }
                    return RoutePoint.builder()
                            .missionId(dto.getMissionId())
                            .x(dto.getX())
                            .y(dto.getY())
                            .z(dto.getZ())
                            .build();
                })
                .toList();

        routePointRepository.saveAll(routePoints);
    }
}