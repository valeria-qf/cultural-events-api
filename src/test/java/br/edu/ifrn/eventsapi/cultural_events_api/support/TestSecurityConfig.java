package br.edu.ifrn.eventsapi.cultural_events_api.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Instant;
import java.util.Map;

@TestConfiguration
@EnableMethodSecurity
@Profile("test")
public class TestSecurityConfig {

    @Bean
    @Primary
    JwtDecoder jwtDecoder() {
        return token -> Jwt.withTokenValue(token)
                .header("alg", "none")
                .claims(c -> c.putAll(Map.of(
                        "sub", "test-user",
                        "iat", Instant.now().getEpochSecond(),
                        "exp", Instant.now().plusSeconds(3600).getEpochSecond()
                )))
                .build();
    }

    @Bean
    SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**", "/v3/api-docs/**", "/swagger", "/swagger/**", "/error"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/events/**", "/api/v1/venues/**", "/api/v1/sessions/**"
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

                .oauth2ResourceServer(oauth -> oauth.jwt());

        return http.build();
    }
}
