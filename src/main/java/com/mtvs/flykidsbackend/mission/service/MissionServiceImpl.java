package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.dto.MissionRequestDto;
import com.mtvs.flykidsbackend.mission.dto.MissionResponseDto;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 미션 관리 서비스 구현체
 * - 미션 등록, 수정, 삭제, 조회 기능을 처리한다
 * - Controller와 Repository 사이의 비즈니스 로직을 담당한다
 */
@Service
@RequiredArgsConstructor
public class MissionServiceImpl implements MissionService {

    private final MissionRepository missionRepository;

    /**
     * 미션 등록
     * @param requestDto 미션 등록 요청 DTO
     * @return 등록된 미션 정보 반환
     */
    @Override
    public MissionResponseDto createMission(MissionRequestDto requestDto) {
        Mission mission = Mission.builder()
                .title(requestDto.getTitle())
                .timeLimit(requestDto.getTimeLimit())
                .type(requestDto.getType())
                .build();

        Mission saved = missionRepository.save(mission);
        return MissionResponseDto.from(saved);
    }

    /**
     * 미션 수정
     * @param id 수정 대상 미션 ID
     * @param requestDto 수정할 내용 DTO
     * @return 수정된 미션 정보 반환
     * @throws IllegalArgumentException 존재하지 않는 미션 ID일 경우 예외 발생
     */
    @Override
    public MissionResponseDto updateMission(Long id, MissionRequestDto requestDto) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션이 존재하지 않습니다."));

        mission = Mission.builder()
                .id(mission.getId()) // 기존 ID 유지
                .title(requestDto.getTitle())
                .timeLimit(requestDto.getTimeLimit())
                .type(requestDto.getType())
                .build();

        Mission updated = missionRepository.save(mission);
        return MissionResponseDto.from(updated);
    }

    /**
     * 미션 삭제
     * @param id 삭제할 미션 ID
     * @throws IllegalArgumentException 존재하지 않는 미션 ID일 경우 예외 발생
     */
    @Override
    public void deleteMission(Long id) {
        if (!missionRepository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 미션이 존재하지 않습니다.");
        }
        missionRepository.deleteById(id);
    }

    /**
     * 단일 미션 조회
     * @param id 조회할 미션 ID
     * @return 조회된 미션 정보
     * @throws IllegalArgumentException 존재하지 않는 미션 ID일 경우 예외 발생
     */
    @Override
    public MissionResponseDto getMission(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션이 존재하지 않습니다."));
        return MissionResponseDto.from(mission);
    }

    /**
     * 전체 미션 목록 조회
     * @return 전체 미션 리스트
     */
    @Override
    public List<MissionResponseDto> getAllMissions() {
        return missionRepository.findAll()
                .stream()
                .map(MissionResponseDto::from)
                .collect(Collectors.toList());
    }
}
