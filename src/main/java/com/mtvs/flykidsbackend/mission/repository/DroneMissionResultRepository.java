package com.mtvs.flykidsbackend.mission.repository;

import com.mtvs.flykidsbackend.mission.entity.DroneMissionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * 특정 미션에 대해 점수 기준 상위 10명의 결과 조회
     *
     * - 미션 ID를 기준으로 필터링
     * - 점수를 기준으로 내림차순 정렬
     * - 상위 10개의 결과만 반환 (리더보드 TOP10 용도)
     *
     * @param missionId 조회할 미션 ID
     * @return 점수 상위 10명의 미션 결과 리스트
     */
    List<DroneMissionResult> findTop10ByMissionIdOrderByScoreDesc(Long missionId);

    /**
     * 특정 유저의 평균 점수 조회
     *
     * @param userId 유저 ID
     * @return 평균 점수 (null 가능)
     */
    @Query("SELECT AVG(r.score) FROM DroneMissionResult r WHERE r.userId = :userId")
    Double findAverageScoreByUserId(@Param("userId") Long userId);

    /**
     * 특정 유저의 총 비행 시간 조회
     *
     * @param userId 유저 ID
     * @return 총 비행 시간 (초 단위, null 가능)
     */
    @Query("SELECT SUM(r.totalTime) FROM DroneMissionResult r WHERE r.userId = :userId")
    Double findTotalFlightTimeByUserId(@Param("userId") Long userId);
}
