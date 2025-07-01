package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.dto.DroneMissionResultRequestDto;
import com.mtvs.flykidsbackend.mission.dto.MissionCompleteResponseDto;
import com.mtvs.flykidsbackend.mission.dto.MissionRequestDto;
import com.mtvs.flykidsbackend.mission.dto.MissionResponseDto;
import com.mtvs.flykidsbackend.mission.entity.DroneMissionResult;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.repository.DroneMissionResultRepository;
import com.mtvs.flykidsbackend.mission.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final DroneMissionResultRepository resultRepository;

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

    /**
     * 미션 완료 처리
     * - 점수 계산 및 결과 저장 후 피드백 문장 응답
     */
    @Transactional
    @Override
    public MissionCompleteResponseDto completeMission(Long userId,
                                                      Long missionId,
                                                      DroneMissionResultRequestDto dto) {

        // 1. 미션 존재 확인
        missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션이 존재하지 않습니다."));

        // 2. 점수 계산
        final int MAX_SCORE = 100;
        final int DEVIATION_PENALTY = 5;
        final int COLLISION_PENALTY = 10;

        int deviation = dto.getDeviationCount();
        int collision = dto.getCollisionCount();

        int totalPenalty = (deviation * DEVIATION_PENALTY) + (collision * COLLISION_PENALTY);
        int score = Math.max(0, MAX_SCORE - totalPenalty);

        // 3. 결과 저장
        DroneMissionResult saved = resultRepository.save(
                DroneMissionResult.builder()
                        .userId(userId)
                        .missionId(missionId)
                        .droneId(dto.getDroneId())
                        .totalTime(dto.getTotalTime())
                        .deviationCount(deviation)
                        .collisionCount(dto.getCollisionCount())
                        .score(score)
                        .build()
        );

        // 4. 메시지 생성
        String msg;

        if (deviation == 0 && collision == 0) {
            msg = String.format("미션 완료! %d점입니다. 이탈과 충돌 없이 성공했습니다.", score);
        } else if (deviation == 0) {
            msg = String.format("미션 완료! %d점입니다. 충돌 %d회 발생했습니다.", score, collision);
        } else if (collision == 0) {
            msg = String.format("미션 완료! %d점입니다. 이탈 %d회 발생했습니다.", score, deviation);
        } else {
            msg = String.format("미션 완료! %d점입니다. 이탈 %d회, 충돌 %d회 발생했습니다.", score, deviation, collision);
        }

        // 5. 응답 DTO
        return MissionCompleteResponseDto.builder()
                .score(score)
                .duration(saved.getTotalTime())
                .deviationCount(deviation)
                .message(msg)
                .build();
    }

}
