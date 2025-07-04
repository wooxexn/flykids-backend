package com.mtvs.flykidsbackend.mission.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtvs.flykidsbackend.mission.dto.DroneMissionResultRequestDto;
import com.mtvs.flykidsbackend.mission.model.MissionType;
import com.mtvs.flykidsbackend.user.dto.LoginRequestDto;
import com.mtvs.flykidsbackend.user.dto.TokenResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.Filter;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 실제 서비스 환경과 최대한 비슷한 상태에서
 * DroneMissionResultController의 미션 완료 API를 테스트한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class DroneMissionResultControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 만약 JwtAuthenticationFilter 등 Security Filter가 있다면 주입해야 할 수도 있음
    @Autowired(required = false)
    private Filter springSecurityFilterChain;

    private Long missionId = 1L;

    private Long testUserId = 123L; // 테스트용 userId, 실제 테스트용 DB에 맞게 변경

    private DroneMissionResultRequestDto requestDto;

    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        // 로그인 DTO (실제 테스트용 사용자 정보)
        LoginRequestDto loginDto = new LoginRequestDto("tester1", "tester1234");

        // 로그인 API 호출해서 JWT 토큰 발급받기
        String loginResponse = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        TokenResponseDto tokenResponse = objectMapper.readValue(loginResponse, TokenResponseDto.class);
        accessToken = tokenResponse.getAccessToken();

        // 미션 아이템 결과 생성
        DroneMissionResultRequestDto.MissionItemResult missionItemResult = DroneMissionResultRequestDto.MissionItemResult.builder()
                .missionType(MissionType.COIN)
                .totalTime(150.0)
                .deviationCount(0)
                .collisionCount(0)
                .collectedCoinCount(5)
                .build();

        requestDto = new DroneMissionResultRequestDto();
        requestDto.setDroneId("test-drone-001");
        requestDto.setItemResults(List.of(missionItemResult));
    }

    @Test
    void completeMission_성공() throws Exception {
        mockMvc.perform(post("/api/missions/{missionId}/complete", missionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", allOf(greaterThanOrEqualTo(0), lessThanOrEqualTo(100))))
                .andExpect(jsonPath("$.message", not(emptyString())));
    }
}
