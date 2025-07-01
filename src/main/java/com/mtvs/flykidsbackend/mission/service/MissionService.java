package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.dto.MissionRequestDto;
import com.mtvs.flykidsbackend.mission.dto.MissionResponseDto;

import java.util.List;

/**
 * 미션 관리 서비스 인터페이스
 * - 미션 CRUD 처리 정의
 */
public interface MissionService {

    /** 미션 등록 */
    MissionResponseDto createMission(MissionRequestDto requestDto);

    /** 미션 수정 */
    MissionResponseDto updateMission(Long id, MissionRequestDto requestDto);

    /** 미션 삭제 */
    void deleteMission(Long id);

    /** 미션 단건 조회 */
    MissionResponseDto getMission(Long id);

    /** 전체 미션 목록 조회 */
    List<MissionResponseDto> getAllMissions();
}
