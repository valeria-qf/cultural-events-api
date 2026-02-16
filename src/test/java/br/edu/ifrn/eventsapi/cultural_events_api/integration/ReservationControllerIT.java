package br.edu.ifrn.eventsapi.cultural_events_api.integration;

import br.edu.ifrn.eventsapi.cultural_events_api.support.IntegrationTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReservationControllerIT extends IntegrationTestBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String createBody() throws Exception {
        Map<String, Object> body = Map.of(
                "sessionId", 1,
                "quantity", 1
        );
        return objectMapper.writeValueAsString(body);
    }

    private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor
    jwtWithRealmRoles(String... roles) {

        List<SimpleGrantedAuthority> authorities = Stream.of(roles)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .toList();

        return jwt()
                .jwt(j -> j.claim("realm_access", Map.of("roles", List.of(roles))))
                .authorities(authorities);
    }

    private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor
    jwtWithRolesAndClaims(List<String> roles, Map<String, Object> claims) {

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .toList();

        return jwt()
                .jwt(j -> {
                    j.claim("realm_access", Map.of("roles", roles));
                    claims.forEach(j::claim);
                })
                .authorities(authorities);
    }

    @Test
    void create_should_return_401_without_token() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_should_return_403_when_role_not_allowed() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .with(jwtWithRealmRoles("GUEST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_should_allow_user_role() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .with(jwtWithRolesAndClaims(
                                List.of("USER"),
                                Map.of("email", "user@ifrn.edu.br", "name", "Usuário Teste")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isCreated());
    }

    @Test
    void create_should_fallback_to_preferred_username_when_email_missing() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .with(jwtWithRolesAndClaims(
                                List.of("USER"),
                                Map.of("preferred_username", "valeria", "given_name", "Valéria")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isCreated());
    }

    @Test
    void list_should_require_auth_by_default() throws Exception {
        mockMvc.perform(get("/api/v1/reservations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void list_should_work_with_auth() throws Exception {
        mockMvc.perform(get("/api/v1/reservations")
                        .with(jwtWithRealmRoles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void cancel_should_require_auth() throws Exception {
        mockMvc.perform(post("/api/v1/reservations/10/cancel"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ticket_should_require_auth() throws Exception {
        mockMvc.perform(get("/api/v1/reservations/ticket/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void availability_should_require_auth() throws Exception {
        mockMvc.perform(get("/api/v1/reservations/availability/1"))
                .andExpect(status().isUnauthorized());
    }
}
