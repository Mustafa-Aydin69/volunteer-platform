package com.imece.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// springdoc builds the endpoint list from the controllers by itself; this bean only adds
// the title and tells Swagger UI how our JWT is sent, which enables the "Authorize" button.
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI imeceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("İmece API")
                        .description("Gönüllülük ve Sosyal Sorumluluk Projeleri Platformu")
                        .version("v1"))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                // Applied to every endpoint so the token is sent once it is entered; public
                // endpoints simply ignore it.
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
