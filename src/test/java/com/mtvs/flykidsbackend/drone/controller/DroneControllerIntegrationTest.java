package com.mtvs.flykidsbackend.drone.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtvs.flykidsbackend.drone.dto.DronePositionRequestDto;
import com.mtvs.flykidsbackend.drone.dto.RoutePointRequestDto;
import com.mtvs.flykidsbackend.drone.service.RoutePointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DroneController의 통합 테스트 클래스
 *
 * 실제 HTTP 요청을 MockMvc를 통해 보내고, 서비스 계층과 DB까지 연동된 상태에서
 * 드론 위치 기록 API의 정상 동작 여부를 검증한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class DroneControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoutePointService routePointService;

    /**
     * 드론 위치 기록 API 정상 호출 테스트
     *
     *  - /api/drone/position-log 엔드포인트에 POST 요청
     *  - 요청 바디에 드론 위치 및 상태 정보 포함
     *  - 응답 상태 200 OK 예상
     *  - 응답 JSON 내 status 필드는 "OK", "WARNING", 또는 "COLLISION" 중 하나여야 함
     *  - 응답 메시지는 빈 문자열이 아니어야 함
     */
    @BeforeEach
    void setup() {
        // 예) 미션 1에 대한 기준 경로 좌표 2개 등록
        List<RoutePointRequestDto> points = List.of(
                new RoutePointRequestDto(1L, 0.0, 0.0, 0.0, 0.0),
                new RoutePointRequestDto(1L, 10.0, 0.0, 0.0, 90.0)
        );
        routePointService.saveRoutePoints(points);
    }

    @Test
    void logDronePosition_성공() throws Exception {
        DronePositionRequestDto dto = new DronePositionRequestDto();
        dto.setX(10.0);
        dto.setY(5.0);
        dto.setZ(3.0);
        dto.setRotationY(90.0);
        dto.setDroneId("1");
        dto.setMissionId(1L);

        mockMvc.perform(post("/api/drone/position-log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", anyOf(is("OK"), is("WARNING"), is("COLLISION"))))
                .andExpect(jsonPath("$.message", not(emptyString())));
    }
}
