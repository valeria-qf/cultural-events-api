package br.edu.ifrn.eventsapi.cultural_events_api.support;

import br.edu.ifrn.eventsapi.cultural_events_api.security.KeycloakRoleConverter;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(IntegrationTestBase.SecurityTestConfig.class)
public abstract class IntegrationTestBase {

    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("cultural_events_test")
                    .withUsername("test")
                    .withPassword("test");

    @BeforeAll
    static void start() {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class SecurityTestConfig {

        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
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
