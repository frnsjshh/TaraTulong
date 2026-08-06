package com.francis.taratulong.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "BearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("TaraTulong Management REST API")
                        .description("Interactive API documentation and schema exploration for the TaraTulong volunteer and organization ecosystem.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("TaraTulong Engineering Team")
                                .email("support@taratulong.org")
                                .url("https://taratulong.org"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                
                // 1. Add Security Requirement globally so protected endpoints display the lock (padlock) icon
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                
                // 2. Configure the JWT Bearer token authentication scheme in components
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token obtained from the /api/v1/auth endpoint. (No need to type 'Bearer ' before the token!).")));
    }
}
