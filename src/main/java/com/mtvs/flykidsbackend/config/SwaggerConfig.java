package com.mtvs.flykidsbackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    /**
     * Swagger용 OpenAPI 설정
     * - 제목, 설명, 버전 정보 구성
     */
    @Bean
    public OpenAPI flyKidsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FlyKids API 문서")
                        .description("FlyKids 프로젝트 – 외부 클라이언트를 위한 인증 API 명세서")
                        .version("v1.0.0"));
    }
}