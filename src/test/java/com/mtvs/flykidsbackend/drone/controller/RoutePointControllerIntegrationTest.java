package com.mtvs.flykidsbackend.drone.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtvs.flykidsbackend.drone.dto.RoutePointRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RoutePointController의 통합 테스트 클래스
 *
 * 기준 경로 좌표 등록 및 조회 API의 정상 동작을 검증한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class RoutePointControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 기준 경로 좌표 여러 개 등록 API 정상 호출 테스트
     *
     *  - /api/route/points 에 POST 요청
     *  - 요청 바디에 여러 기준 경로 좌표 DTO 리스트 포함
     *  - 응답 상태 200 OK 예상
     *  - 응답 메시지는 "기준 경로가 저장되었습니다." 여야 함
     */
    @Test
    void saveRoutePoints_성공() throws Exception {
        List<RoutePointRequestDto> pointList = List.of(
                new RoutePointRequestDto(1L, 2.0, 3.0, 45.0, 1L),
                new RoutePointRequestDto(4L, 5.0, 6.0, 90.0, 1L)
        );

        mockMvc.perform(post("/api/route/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pointList)))
                .andExpect(status().isOk())
                .andExpect(content().string("기준 경로가 저장되었습니다."));
    }

    /**
     * 미션 ID로 기준 경로 조회 API 정상 호출 테스트
     *
     *  - /api/route/points 에 GET 요청
     *  - missionId 쿼리 파라미터 포함
     *  - 응답 상태 200 OK 예상
     *  - 응답 바디는 배열 형태여야 함
     */
    @Test
    void getRoutePoints_성공() throws Exception {
        Long missionId = 1L;

        mockMvc.perform(get("/api/route/points")
                        .param("missionId", String.valueOf(missionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
