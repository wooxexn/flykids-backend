package com.mtvs.flykidsbackend.drone.repository;

import com.mtvs.flykidsbackend.drone.entity.RoutePoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 기준 경로 포인트 레포지토리
 *
 * 특정 미션 ID에 해당하는 기준 경로 좌표 목록을 조회한다.
 */
public interface RoutePointRepository extends JpaRepository<RoutePoint, Long> {

    /**
     * 특정 미션 ID에 해당하는 기준 경로 포인트들을 반환한다.
     *
     * @param missionId 기준 경로가 속한 미션 ID
     * @return 해당 미션의 모든 기준 좌표 리스트
     */
    List<RoutePoint> findByMissionId(Long missionId);
}
