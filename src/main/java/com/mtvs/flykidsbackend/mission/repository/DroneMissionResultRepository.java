package com.mtvs.flykidsbackend.mission.repository;

import com.mtvs.flykidsbackend.mission.entity.DroneMissionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 미션 결과 저장용 리포지토리
 * - 유저별, 미션별 결과 조회 등 확장을 위해 메서드 정의 가능
 */
@Repository
public interface DroneMissionResultRepository extends JpaRepository<DroneMissionResult, Long> {

    /**
     * 특정 유저가 수행한 전체 미션 결과 조회
     */
    List<DroneMissionResult> findByUserId(Long userId);

    /**
     * 특정 미션에 대한 모든 유저의 결과 조회 (리더보드 등에서 활용)
     */
    List<DroneMissionResult> findByMissionId(Long missionId);
}
