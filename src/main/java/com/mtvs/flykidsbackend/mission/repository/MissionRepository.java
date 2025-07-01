package com.mtvs.flykidsbackend.mission.repository;

import com.mtvs.flykidsbackend.mission.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 미션 관리용 리포지토리
 * - 미션 등록, 조회, 수정, 삭제에 사용
 */
@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {
}
