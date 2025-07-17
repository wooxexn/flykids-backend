package com.mtvs.flykidsbackend.domain.mission.service;

import com.mtvs.flykidsbackend.common.AudioFilePath;
import com.mtvs.flykidsbackend.domain.mission.dto.DroneMissionResultRequestDto;
import com.mtvs.flykidsbackend.domain.mission.dto.MissionCompleteResponseDto;
import com.mtvs.flykidsbackend.domain.mission.dto.MissionRequestDto;
import com.mtvs.flykidsbackend.domain.mission.dto.MissionResponseDto;
import com.mtvs.flykidsbackend.domain.mission.entity.DroneMissionResult;
import com.mtvs.flykidsbackend.domain.mission.entity.Mission;
import com.mtvs.flykidsbackend.domain.mission.model.MissionResultStatus;
import com.mtvs.flykidsbackend.domain.mission.model.MissionType;
import com.mtvs.flykidsbackend.domain.user.entity.User;
import com.mtvs.flykidsbackend.domain.mission.repository.DroneMissionResultRepository;
import com.mtvs.flykidsbackend.domain.mission.repository.MissionRepository;
import com.mtvs.flykidsbackend.domain.user.model.UserMissionStatus;
import com.mtvs.flykidsbackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 미션 관리 서비스 구현 클래스
 * - 미션 등록, 수정, 삭제, 조회, 완료 처리 비즈니스 로직 담당
 * - 유저 미션 진행 상태 처리 및 결과 저장 포함
 */
@Service
@RequiredArgsConstructor
public class MissionServiceImpl implements MissionService {

    private final MissionRepository missionRepository;
    private final DroneMissionResultRepository resultRepository;
    private final ScoreCalculator scoreCalculator;
    private final UserMissionProgressService userMissionProgressService;
    private final UserRepository userRepository;

    /**
     * 미션 등록
     *
     * @param dto    등록할 미션 정보
     * @param userId 등록 요청한 사용자 ID (현재는 사용하지 않음)
     * @return 등록된 미션 정보
     */
    @Override
    @Transactional
    public MissionResponseDto createMission(MissionRequestDto dto, Long userId) {
        Mission mission = Mission.builder()
                .title(dto.getTitle())
                .timeLimit(dto.getTimeLimit())
                .type(dto.getType())
                .totalCoinCount(dto.getTotalCoinCount())
                .introMessage(dto.getIntroMessage())
                .build();
        return MissionResponseDto.from(missionRepository.save(mission));
    }

    /**
     * 미션 정보 수정
     *
     * @param id     수정할 미션 ID
     * @param dto    수정할 내용
     * @return 수정된 미션 정보
     */
    @Override
    @Transactional
    public MissionResponseDto updateMission(Long id, MissionRequestDto dto) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션이 존재하지 않습니다."));
        mission.setTitle(dto.getTitle());
        mission.setTimeLimit(dto.getTimeLimit());
        mission.setType(dto.getType());
        mission.setTotalCoinCount(dto.getTotalCoinCount());
        mission.setIntroMessage(dto.getIntroMessage());
        return MissionResponseDto.from(mission);
    }

    /**
     * 미션 삭제
     */
    @Override
    public void deleteMission(Long id) {
        if (!missionRepository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 미션이 존재하지 않습니다.");
        }
        missionRepository.deleteById(id);
    }

    /**
     * 특정 미션 조회
     */
    @Override
    public MissionResponseDto getMission(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션이 존재하지 않습니다."));
        return MissionResponseDto.from(mission);
    }

    /**
     * 전체 미션 목록 조회
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
     *
     * <처리 절차>
     * 1. 유저 및 미션 조회
     * 2. 점수 계산 및 성공 여부 판단
     * 3. 결과 저장 (DroneMissionResult)
     * 4. 성공 시 다음 미션 오픈
     * 5. 클라이언트용 텍스트 메시지 구성
     * 6. 성공/실패 상태에 따른 고정 음성(mp3) URL 반환
     *
     * @param userId    수행한 유저 ID
     * @param missionId 완료한 미션 ID
     * @param dto       미션 수행 결과 데이터
     * @return 점수, 메시지, 음성 URL 등을 포함한 결과 응답
     */
    @Transactional
    @Override
    public MissionCompleteResponseDto completeMission(Long userId, Long missionId, DroneMissionResultRequestDto dto) {
        // 1. 미션, 사용자 조회
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션이 존재하지 않습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        MissionType type = mission.getType();
        DroneMissionResultRequestDto.MissionItemResult item = dto.getItemResult();

        // 2. 점수 계산 및 성공 여부 판단
        int score = scoreCalculator.calculateScore(type, dto);
        boolean success = scoreCalculator.isMissionSuccess(type, dto, mission);

        // 3. 결과 저장
        DroneMissionResult result = DroneMissionResult.builder()
                .userId(userId)
                .mission(mission)
                .droneId(dto.getDroneId())
                .totalTime(item.getTotalTime())
                .deviationCount(item.getDeviationCount())
                .collisionCount(item.getCollisionCount())
                .score(score)
                .status(success ? MissionResultStatus.SUCCESS : MissionResultStatus.FAIL)
                .build();

        DroneMissionResult saved = resultRepository.save(result);

        // 4. 성공 시 다음 미션 자동 오픈 처리
        if (success) {
            getNextMission(mission).ifPresent(nextMission -> {
                nextMission.unlock();
                missionRepository.save(nextMission);
                userMissionProgressService.createIfNotExist(user, nextMission, UserMissionStatus.READY);
            });
        }

        // 5. 텍스트 피드백 메시지 구성
        String feedbackText = switch (type) {
            case COIN -> success ? "코인을 다 모았어요! 대단해요!" : "코인을 몇 개 놓쳤지만 잘했어요!";
            case OBSTACLE -> success ? "장애물을 멋지게 피했어요!" : "조금 부딪혔지만 끝까지 도전했어요!";
            case PHOTO -> success ? "사진을 정확히 찍었어요!" : "사진 찍으려는 노력이 멋졌어요!";
            default -> "미션을 완료했어요!";
        };

        String summaryMsg = success ?
                "미션을 완벽하게 해냈어요! 정말 멋진 드론 조종자예요!" :
                "조금 어려웠지만 포기하지 않았어요! 다음엔 더 잘할 수 있어요!";

        String fullMsg = summaryMsg + "\n" + feedbackText;
        String cleanMsg = sanitizeForTTS(fullMsg);

        // 6. S3 고정 음성 URL 선택
        String audioUrl = getFeedbackAudioUrl(missionId, success);

        // 최종 응답 객체 반환
        return MissionCompleteResponseDto.builder()
                .score(score)
                .duration(saved.getTotalTime())
                .deviationCount(saved.getDeviationCount())
                .collisionCount(saved.getCollisionCount())
                .success(success)
                .message(cleanMsg)
                .rawMessage(fullMsg)
                .audioUrl(audioUrl)
                .build();
    }

    /**
     * 미션 ID로 조회
     */
    @Override
    public Optional<Mission> findById(Long id) {
        return missionRepository.findById(id);
    }

    /**
     * 미션 ID로 Entity 직접 반환 (Optional 아님)
     */
    @Override
    public Mission getMissionEntity(Long id) {
        return missionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션이 존재하지 않습니다."));
    }

    /**
     * TTS용 문장 정제
     * - 특수 문자 제거, 공백 정리
     *
     * @param text 원본 메시지
     * @return 정제된 텍스트
     */
    private String sanitizeForTTS(String text) {
        return text.replaceAll("[^\\p{L}\\p{N}\\s.!?]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * 고정 음성(mp3) 파일 경로 반환
     *
     * @param missionId 미션 ID
     * @param success   성공 여부
     * @return S3 mp3 파일 URL
     */
    private String getFeedbackAudioUrl(Long missionId, boolean success) {
        if (success) return AudioFilePath.MISSION_SUCCESS;

        return switch (missionId.intValue()) {
            case 1 -> AudioFilePath.MISSION1_FAIL;
            case 2 -> AudioFilePath.MISSION2_FAIL;
            case 3 -> AudioFilePath.MISSION3_FAIL;
            default -> AudioFilePath.MISSION_FAIL_COMMON;
        };
    }

    /**
     * 다음 미션 조회
     * - 현재 미션의 orderIndex 기준으로 다음 순서를 가진 미션 반환
     */
    @Override
    public Optional<Mission> getNextMission(Mission currentMission) {
        Integer nextOrderIndex = currentMission.getOrderIndex() + 1;
        return missionRepository.findByOrderIndex(nextOrderIndex);
    }
}
