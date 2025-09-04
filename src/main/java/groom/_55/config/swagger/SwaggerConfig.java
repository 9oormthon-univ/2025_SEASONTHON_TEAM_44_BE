package groom._55.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // 보안 스키마 정의 (JWT Bearer Token)
        String jwtSchemeName = "bearerAuth";
        SecurityScheme securityScheme = new SecurityScheme()
                .name(jwtSchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER); // 토큰을 HTTP 헤더에 담아 보냅니다.

        // 보안 요구사항 정의 (모든 API에 JWT 적용)
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        return new OpenAPI()
                .info(new Info()
                        .title("다시온 API")
                        .description("[2025 kakao X groom 시즌톤] 44팀 다시온 BE")
                        .version("v1.0.0"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Server")
                ))
                // Components에 보안 스키마를 추가
                .components(new Components().addSecuritySchemes(jwtSchemeName, securityScheme))
                // 전체 API에 보안 요구사항을 적용
                .addSecurityItem(securityRequirement);
    }
}
