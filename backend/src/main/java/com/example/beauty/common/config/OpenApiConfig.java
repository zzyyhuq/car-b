package com.example.beauty.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 文档配置。
 * 启动后访问 http://localhost:8081/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI beautyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("某4S店汽车美容服务系统 API")
                        .description("车主端小程序与管理端共用的后端接口。先调 /api/auth/login 拿到 token，"
                                + "点右上角 Authorize 填入即可调试需要登录的接口。")
                        .version("0.1.0"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("登录接口返回的 token，不需要自己加 Bearer 前缀")));
    }
}
