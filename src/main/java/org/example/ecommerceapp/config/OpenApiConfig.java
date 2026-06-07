package org.example.ecommerceapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        String securitySchemeName = "bearerAuth";

        return new OpenAPI()

                // API Information
                .info(new Info()
                        .title("E-Commerce REST API")
                        .description("Spring Boot E-Commerce Application with JWT Authentication")
                        .version("1.0"))

                // Apply JWT security globally
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(securitySchemeName)
                )

                // Define JWT authentication scheme
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }
}