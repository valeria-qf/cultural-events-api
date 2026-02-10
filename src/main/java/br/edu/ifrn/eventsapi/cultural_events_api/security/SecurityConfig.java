package br.edu.ifrn.eventsapi.cultural_events_api.security;

import br.edu.ifrn.eventsapi.cultural_events_api.service.AppUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AppUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Swagger público (inclui path custom /swagger)
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger",
                                "/swagger/**"
                        ).permitAll()

                        // Auth público
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // GET público
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/events/**",
                                "/api/v1/venues/**",
                                "/api/v1/sessions/**"
                        ).permitAll()

                        // Reservas -> criar exige auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/reservations")
                        .hasAnyRole("USER", "ADMIN", "ORGANIZER")

                        // CRUD -> admin/organizer
                        .requestMatchers(HttpMethod.POST, "/api/v1/events/**", "/api/v1/venues/**", "/api/v1/sessions/**")
                        .hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/events/**", "/api/v1/venues/**", "/api/v1/sessions/**")
                        .hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/events/**", "/api/v1/venues/**", "/api/v1/sessions/**")
                        .hasAnyRole("ADMIN", "ORGANIZER")

                        .anyRequest().authenticated()
                )
                .userDetailsService(userDetailsService)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
