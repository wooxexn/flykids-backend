package com.mtvs.flykidsbackend.domain.mission.service;

import com.mtvs.flykidsbackend.domain.mission.dto.DroneMissionResultRequestDto;
import com.mtvs.flykidsbackend.domain.mission.dto.MissionCompleteResponseDto;
import com.mtvs.flykidsbackend.domain.mission.dto.MissionRequestDto;
import com.mtvs.flykidsbackend.domain.mission.dto.MissionResponseDto;
import com.mtvs.flykidsbackend.domain.mission.entity.Mission;

import java.util.List;
import java.util.Optional;

/**
 * 미션 관리 서비스 인터페이스
 * - 미션 CRUD 처리 정의
 */
public interface MissionService {

    /** 미션 등록 */
    MissionResponseDto createMission(MissionRequestDto dto, Long userId);

    /** 미션 수정 */
    MissionResponseDto updateMission(Long id, MissionRequestDto requestDto);

    /** 미션 삭제 */
    void deleteMission(Long id);

    /** 미션 단건 조회 */
    MissionResponseDto getMission(Long id);

    /** 전체 미션 목록 조회 */
    List<MissionResponseDto> getAllMissions();

    /** 미션 완료 처리(결과 저장 + TTS 응답) */
    MissionCompleteResponseDto completeMission(
            Long userId,
            Long missionId,
            DroneMissionResultRequestDto requestDto
    );

    /** 미션 ID로 Mission 엔티티 직접 조회 */
    Optional<Mission> findById(Long id);
    Mission getMissionEntity(Long id);

    Optional<Mission> getNextMission(Mission currentMission);
}
