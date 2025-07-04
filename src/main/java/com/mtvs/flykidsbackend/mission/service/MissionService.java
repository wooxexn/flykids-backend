package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.dto.DroneMissionResultRequestDto;
import com.mtvs.flykidsbackend.mission.dto.MissionCompleteResponseDto;
import com.mtvs.flykidsbackend.mission.dto.MissionRequestDto;
import com.mtvs.flykidsbackend.mission.dto.MissionResponseDto;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.entity.MissionItem;

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
            DroneMissionResultRequestDto requestDto);

    // Mission 엔티티 조회용 메서드
    Optional<Mission> findById(Long id);

    // MissionItem 엔티티 조회용 메서드
    Optional<MissionItem> findMissionItemById(Long id);
}
