package com.mtvs.flykidsbackend.mission.repository;

import com.mtvs.flykidsbackend.mission.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 미션 관리용 리포지토리
 * - 미션 등록, 조회, 수정, 삭제에 사용
 */
@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {

    List<Mission> findAllByOrderByOrderIndexAsc();

    // 순서값으로 특정 미션 조회 (예: 다음 단계 미션 찾기)
    Optional<Mission> findByOrderIndex(Integer orderIndex);

    // 현재 미션 순서보다 큰 locked 미션 중 가장 빠른 미션 찾기
    Optional<Mission> findFirstByOrderIndexGreaterThanAndLockedIsTrueOrderByOrderIndexAsc(Integer currentOrderIndex);

}
