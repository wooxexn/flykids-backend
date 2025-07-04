package com.mtvs.flykidsbackend.mission.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtvs.flykidsbackend.mission.dto.MissionRequestDto;
import com.mtvs.flykidsbackend.mission.dto.MissionRequestDto.MissionItemDto;
import com.mtvs.flykidsbackend.mission.model.MissionType;
import com.mtvs.flykidsbackend.user.dto.LoginRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserMissionProgressController 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
public class UserMissionProgressControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String accessToken;
    private Long missionId;
    private Long missionItemId;

    @BeforeEach
    void setUp() throws Exception {
        // 1. 로그인해서 accessToken 획득
        LoginRequestDto loginDto = new LoginRequestDto("tester1", "tester1234");
        String loginResponse = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        accessToken = objectMapper.readTree(loginResponse).get("accessToken").asText();

        // 2. 테스트용 미션 생성 (미션 + 아이템 1개 이상 포함)
        MissionRequestDto missionRequestDto = MissionRequestDto.builder()
                .title("테스트 미션")
                .items(List.of(
                        MissionItemDto.builder()
                                .title("테스트 아이템")
                                .type(MissionType.COIN)
                                .timeLimit(300)
                                .totalCoinCount(10)
                                .build()
                ))
                .build();

        String missionJson = objectMapper.writeValueAsString(missionRequestDto);

        String missionResponse = mockMvc.perform(post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missionJson)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // missionId, missionItemId 추출
        missionId = objectMapper.readTree(missionResponse).get("id").asLong();
        missionItemId = objectMapper.readTree(missionResponse)
                .get("items").get(0).get("id").asLong();
    }

    @Test
    void getProgressList_success() throws Exception {
        mockMvc.perform(get("/api/user-mission-progress/missions/{missionId}", missionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updateProgress_success() throws Exception {
        mockMvc.perform(post("/api/user-mission-progress/missions/{missionId}/items/{missionItemId}",
                        missionId, missionItemId)
                        .param("status", "COMPLETED")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // 상태 업데이트 후 조회해서 반영됐는지 간단 검증 (선택 사항)
        MvcResult result = mockMvc.perform(get("/api/user-mission-progress/missions/{missionId}", missionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        String responseStr = result.getResponse().getContentAsString();
        assertThat(responseStr).contains("COMPLETED");
    }
}
