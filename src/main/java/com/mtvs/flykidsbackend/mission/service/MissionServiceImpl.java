package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.mission.dto.DroneMissionResultRequestDto;
import com.mtvs.flykidsbackend.mission.dto.MissionCompleteResponseDto;
import com.mtvs.flykidsbackend.mission.dto.MissionRequestDto;
import com.mtvs.flykidsbackend.mission.dto.MissionResponseDto;
import com.mtvs.flykidsbackend.mission.entity.DroneMissionResult;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.entity.MissionItem;
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
    private final DroneMissionResultService resultService;

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
                .build();

        // MissionItem 리스트 생성
        List<MissionItem> items = requestDto.getItems().stream()
                .map(itemDto -> MissionItem.builder()
                        .type(itemDto.getType())
                        .mission(mission)  // 양방향 연관관계 설정
                        .build())
                .collect(Collectors.toList());

        mission.setItems(items);

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

        mission.setTitle(requestDto.getTitle());
        mission.setTimeLimit(requestDto.getTimeLimit());

        // 기존 items 삭제 후 새로운 아이템 리스트 재설정
        mission.getItems().clear();

        List<MissionItem> items = requestDto.getItems().stream()
                .map(itemDto -> MissionItem.builder()
                        .type(itemDto.getType())
                        .mission(mission)
                        .build())
                .collect(Collectors.toList());

        mission.setItems(items);

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
    public MissionCompleteResponseDto completeMission(Long userId, Long missionId, DroneMissionResultRequestDto dto) {

        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션이 존재하지 않습니다."));

        int totalScore = 0;
        int totalDeviation = 0;
        int totalCollision = 0;

        for (DroneMissionResultRequestDto.MissionItemResult itemResult : dto.getItemResults()) {
            totalScore += resultService.calculateScore(
                    itemResult.getType(),
                    itemResult.getItemTime(),
                    itemResult.getDeviationCount(),
                    itemResult.getCollisionCount()
            );
            totalDeviation += itemResult.getDeviationCount();
            totalCollision += itemResult.getCollisionCount();
        }

        totalScore = Math.min(totalScore, 100);

        DroneMissionResult saved = resultRepository.save(
                DroneMissionResult.builder()
                        .userId(userId)
                        .missionId(missionId)
                        .droneId(dto.getDroneId())
                        .totalTime(dto.getTotalTime())
                        .deviationCount(totalDeviation)
                        .collisionCount(totalCollision)
                        .score(totalScore)
                        .build()
        );

        // 메시지 생성
        String msg;
        if (totalDeviation == 0 && totalCollision == 0) {
            msg = String.format("미션 완료! %d점입니다. 이탈과 충돌 없이 성공했습니다.", totalScore);
        } else if (totalDeviation == 0) {
            msg = String.format("미션 완료! %d점입니다. 충돌 %d회 발생했습니다.", totalScore, totalCollision);
        } else if (totalCollision == 0) {
            msg = String.format("미션 완료! %d점입니다. 이탈 %d회 발생했습니다.", totalScore, totalDeviation);
        } else {
            msg = String.format("미션 완료! %d점입니다. 이탈 %d회, 충돌 %d회 발생했습니다.", totalScore, totalDeviation, totalCollision);
        }

        return MissionCompleteResponseDto.builder()
                .score(totalScore)
                .duration(saved.getTotalTime())
                .deviationCount(totalDeviation)
                .collisionCount(totalCollision)
                .message(msg)
                .build();
    }
}
