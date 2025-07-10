package com.mtvs.flykidsbackend.mission.service;

import com.mtvs.flykidsbackend.ai.dto.TtsRequestDto;
import com.mtvs.flykidsbackend.ai.dto.TtsResponseDto;
import com.mtvs.flykidsbackend.ai.service.TtsService;
import com.mtvs.flykidsbackend.mission.dto.*;
import com.mtvs.flykidsbackend.mission.entity.DroneMissionResult;
import com.mtvs.flykidsbackend.mission.entity.Mission;
import com.mtvs.flykidsbackend.mission.model.MissionResultStatus;
import com.mtvs.flykidsbackend.mission.model.MissionType;
import com.mtvs.flykidsbackend.user.entity.User;
import com.mtvs.flykidsbackend.mission.repository.DroneMissionResultRepository;
import com.mtvs.flykidsbackend.mission.repository.MissionRepository;
import com.mtvs.flykidsbackend.user.model.UserMissionStatus;
import com.mtvs.flykidsbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 미션 관리 서비스 구현체
 * - 미션 등록, 수정, 삭제, 조회, 완료 기능을 처리한다
 * - Controller와 Repository 사이의 비즈니스 로직을 담당한다
 */
@Service
@RequiredArgsConstructor
public class MissionServiceImpl implements MissionService {

    private final MissionRepository missionRepository;
    private final DroneMissionResultRepository resultRepository;
    private final ScoreCalculator scoreCalculator;
    private final UserMissionProgressService userMissionProgressService;
    private final UserRepository userRepository;
    private final TtsService ttsService;

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

    @Override
    public void deleteMission(Long id) {
        if (!missionRepository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 미션이 존재하지 않습니다.");
        }
        missionRepository.deleteById(id);
    }

    @Override
    public MissionResponseDto getMission(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션이 존재하지 않습니다."));
        return MissionResponseDto.from(mission);
    }

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
     * <기능 요약>
     * - 클라이언트로부터 전달받은 미션 수행 결과(드론 ID, 수행 시간, 결과 등)를 바탕으로
     *   점수를 계산하고 성공 여부를 판단한다.
     * - 결과를 DB에 저장하고, 최종 응답 메시지를 생성하여 반환한다.
     *
     * <처리 순서>
     * 1. 미션 정보 조회
     * 2. 미션 유형에 따른 점수 계산 및 성공 여부 판단
     * 3. 결과(DroneMissionResult) 저장
     * 4. 사용자에게 전달할 피드백 메시지 구성 (AI/TTS용)
     * 5. MissionCompleteResponseDto 응답 반환
     *
     * @param userId    JWT로부터 전달된 사용자 ID
     * @param missionId 수행한 미션의 ID
     * @param dto       미션 수행 결과 데이터 (단일 미션 아이템 정보 포함)
     * @return MissionCompleteResponseDto (점수, 성공 여부, 안내 메시지 포함)
     * @throws IllegalArgumentException 존재하지 않는 미션 ID인 경우
     */
    @Transactional
    @Override
    public MissionCompleteResponseDto completeMission(Long userId, Long missionId, DroneMissionResultRequestDto dto) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션이 존재하지 않습니다."));

        // User 객체 조회 추가
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        MissionType type = mission.getType();

        // 단일 MissionItemResult 필드 사용
        DroneMissionResultRequestDto.MissionItemResult item = dto.getItemResult();

        // 점수 계산
        int score = scoreCalculator.calculateScore(type, dto);

        // 성공 여부 판별
        boolean success = scoreCalculator.isMissionSuccess(type, dto, mission);

        // 결과 저장
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

        // 성공 시 다음 미션 자동 오픈 처리
        if (success) {
            getNextMission(mission).ifPresent(nextMission -> {
                // 1. 다음 미션을 오픈 처리
                nextMission.unlock();
                missionRepository.save(nextMission); // 변경사항 저장

                // 2. 유저 진행 상태 초기화 (enum으로 수정)
                userMissionProgressService.createIfNotExist(user, nextMission, UserMissionStatus.READY);
            });
        }

        // 메시지 생성
        String finalMsg = switch (type) {
            case COIN -> success ? "코인을 다 모았어요! 대단해요!" : "코인을 몇 개 놓쳤지만 잘했어요!";
            case OBSTACLE -> success ? "장애물을 멋지게 피했어요!" : "조금 부딪혔지만 끝까지 도전했어요!";
            case PHOTO -> success ? "사진을 정확히 찍었어요!" : "사진 찍으려는 노력이 멋졌어요!";
            default -> "미션을 완료했어요!";
        };

        String baseSuccessMsg = success ?
                "미션을 완벽하게 해냈어요! 정말 멋진 드론 조종자예요!" :
                "조금 어려웠지만 포기하지 않았어요! 다음엔 더 잘할 수 있어요!";

        String fullMsg = baseSuccessMsg + "\n" + finalMsg;
        String cleanMsg = cleanForTTS(fullMsg);

        //TTS 요청 보내기
        TtsRequestDto ttsRequest = TtsRequestDto.builder()
                .userId(user.getUsername()) // 또는 userId.toString()
                .missionId(missionId)
                .status(success ? "success" : "fail")
                .message(cleanMsg)
                .build();

        TtsResponseDto ttsResponse = ttsService.sendTtsRequest(ttsRequest);

        return MissionCompleteResponseDto.builder()
                .score(score)
                .duration(saved.getTotalTime())
                .deviationCount(saved.getDeviationCount())
                .collisionCount(saved.getCollisionCount())
                .success(success)
                .message(cleanMsg)
                .rawMessage(fullMsg)
                .audioUrl(ttsResponse.getAudioUrl())
                .build();
    }

    @Override
    public Optional<Mission> findById(Long id) {
        return missionRepository.findById(id);
    }

    @Override
    public Mission getMissionEntity(Long id) {
        return missionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션이 존재하지 않습니다."));
    }

    /**
     * TTS(Text-To-Speech)용 문자열 정제 함수
     * - 특수기호, 줄바꿈 등을 제거하여 음성 합성에 적합한 문장으로 변환
     */
    private String cleanForTTS(String text) {
        return text.replaceAll("[^\\p{L}\\p{N}\\s.!?]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * 현재 미션 다음 순서의 미션 조회
     * - orderIndex를 기반으로 다음 미션을 찾아 반환
     * - 없으면 Optional.empty()
     */
    @Override
    public Optional<Mission> getNextMission(Mission currentMission) {
        Integer nextOrderIndex = currentMission.getOrderIndex() + 1;
        return missionRepository.findByOrderIndex(nextOrderIndex);
    }
}
