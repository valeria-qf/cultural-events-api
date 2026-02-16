package br.edu.ifrn.eventsapi.cultural_events_api.support;

import br.edu.ifrn.eventsapi.cultural_events_api.security.KeycloakRoleConverter;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(IntegrationTestBase.SecurityTestConfig.class)
public abstract class IntegrationTestBase {

    @TestConfiguration
    @EnableMethodSecurity
    public static class SecurityTestConfig {

        @Bean
        public JwtDecoder jwtDecoder() {
            return token -> Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("sub", "test")
                    .build();
        }

        @Bean
        public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            JwtAuthenticationConverter jwtAuthConverter = new JwtAuthenticationConverter();
            jwtAuthConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());

            http
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(
                                    "/swagger-ui/**",
                                    "/v3/api-docs/**",
                                    "/swagger",
                                    "/swagger/**",
                                    "/swagger-ui/oauth2-redirect.html",
                                    "/swagger/oauth2-redirect.html",
                                    "/swaggerger/oauth2-redirect.html",
                                    "/error"
                            ).permitAll()

                            .requestMatchers(HttpMethod.GET,
                                    "/api/v1/events/**",
                                    "/api/v1/venues/**",
                                    "/api/v1/sessions/**"
                            ).permitAll()

                            .requestMatchers(HttpMethod.POST, "/api/v1/reservations")
                            .hasAnyRole("USER", "ADMIN", "ORGANIZER")

                            .requestMatchers(HttpMethod.POST, "/api/v1/events/**", "/api/v1/venues/**", "/api/v1/sessions/**")
                            .hasAnyRole("ADMIN", "ORGANIZER")
                            .requestMatchers(HttpMethod.PUT, "/api/v1/events/**", "/api/v1/venues/**", "/api/v1/sessions/**")
                            .hasAnyRole("ADMIN", "ORGANIZER")
                            .requestMatchers(HttpMethod.DELETE, "/api/v1/events/**", "/api/v1/venues/**", "/api/v1/sessions/**")
                            .hasAnyRole("ADMIN", "ORGANIZER")

                            .anyRequest().authenticated()
                    )
                    .oauth2ResourceServer(oauth -> oauth
                            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                    );

            return http.build();
        }
    }
}
