package com.daijia.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口可视化文档配置类：配置 OpenAPI 规范的文档信息
 * 用于生成 Swagger 文档，展示 API 接口的定义和使用方法
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI daijiaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("代驾代办 H5 服务平台 API")
                        .version("1.0.0")
                        .description("客户端 H5、服务商接单 H5、管理后台共用的后端接口"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
