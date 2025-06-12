package com.finance.admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${app.server.url:http://localhost:8080}")
    private String serverUrl;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LifeTech Financial Services API")
                        .description("Comprehensive API for LifeTech Financial Services Platform including client management, authentication, and blockchain integration")
                        .version(appVersion)
                        .contact(new Contact()
                                .name("LifeTech Development Team")
                                .email("dev@lifetech.com")
                                .url("https://lifetech.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://lifetech.com/license")))
                .servers(List.of(
                        new Server()
                                .url(serverUrl)
                                .description("Development Server"),
                        new Server()
                                .url("https://api.lifetech.com")
                                .description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication", 
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from login endpoint")));
    }
} 