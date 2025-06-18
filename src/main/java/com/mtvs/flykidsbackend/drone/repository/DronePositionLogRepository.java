package com.mtvs.flykidsbackend.drone.repository;

import com.mtvs.flykidsbackend.drone.entity.DronePositionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *  드론 위치 로그 Repository
 *
 * DronePositionLog 엔티티에 대한 DB 접근을 담당하는 JPA Repository 인터페이스
 */
@Repository
public interface DronePositionLogRepository extends JpaRepository<DronePositionLog, Long> {

    // 예시: 특정 미션의 모든 위치 로그 조회
    List<DronePositionLog> findByMissionId(Long missionId);

    // 예시: 특정 드론의 로그 조회
    List<DronePositionLog> findByDroneId(String droneId);
}
