package com.mtvs.flykidsbackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    /**
     * Swagger용 OpenAPI 설정
     * - 기본 API 문서 정보(title, description, version)
     * - JWT 인증용 보안 설정 (Authorize 버튼 활성화)
     */
    @Bean
    public OpenAPI flyKidsOpenAPI() {
        return new OpenAPI()

                // Swagger에서 Authorize 버튼을 활성화시켜 JWT 입력 가능하게 함
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))

                // JWT 인증 방식 설정 (HTTP + Bearer + JWT 포맷)
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)      // HTTP 기반 인증
                                        .scheme("bearer")                    // Bearer 스킴 사용
                                        .bearerFormat("JWT")                 // JWT 포맷 명시
                        )
                )

                // API 문서 메타정보 설정
                .info(new Info()
                        .title("FlyKids API 문서")
                        .description("FlyKids 프로젝트 – 외부 클라이언트를 위한 인증 API 명세서")
                        .version("v1.0.0"));
    }
}
