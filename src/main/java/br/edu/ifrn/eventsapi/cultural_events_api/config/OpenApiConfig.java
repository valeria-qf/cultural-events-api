package br.edu.ifrn.eventsapi.cultural_events_api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Cultural Events API",
                version = "v1",
                description = "API REST para eventos, locais, sessões e reservas"
        ),
        security = { @SecurityRequirement(name = "keycloak") }
)
@SecurityScheme(
        name = "keycloak",
        type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(
                authorizationCode = @OAuthFlow(
                        authorizationUrl = "${app.keycloak.auth-url}",
                        tokenUrl = "${app.keycloak.token-url}"
                )
        )
)
public class OpenApiConfig {}
