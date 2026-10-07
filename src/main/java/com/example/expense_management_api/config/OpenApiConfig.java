package com.example.expense_management_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI expenseManagementOpenAPI() {

        String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Expense Management API")
                        .description(
                            "REST API for managing personal expenses, "
                            + "categories and user accounts."
                        )
                        .version("1.0.0")
                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(securitySchemeName)
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(
                                                    SecurityScheme.Type.HTTP
                                                )
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }
}