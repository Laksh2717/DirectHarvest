package com.directharvest.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI directHarvestOpenApi() {
        String cookieScheme = "cookieAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("DirectHarvest API")
                        .version("v1")
                        .description("API documentation for DirectHarvest backend. Secured endpoints read JWT from HttpOnly cookie: access_token."))
                .addSecurityItem(new SecurityRequirement().addList(cookieScheme))
                .components(new Components()
                        .addSecuritySchemes(cookieScheme, new SecurityScheme()
                                .name("access_token")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)));
    }
}
