package br.edu.ifrn.eventsapi.cultural_events_api.integration.controller;

import br.edu.ifrn.eventsapi.cultural_events_api.support.IntegrationTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReservationControllerIT extends IntegrationTestBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String createBody() throws Exception {
        var body = Map.of(
                "sessionId", 1,
                "quantity", 1
        );
        return objectMapper.writeValueAsString(body);
    }

    private static var jwtWithRealmRoles(String... roles) {
        return jwt().jwt(j -> j.claim("realm_access", Map.of("roles", List.of(roles))));
    }

    private static var jwtWithRolesAndClaims(
            List<String> roles,
            Map<String, Object> claims
    ) {
        return jwt().jwt(j -> {
            j.claim("realm_access", Map.of("roles", roles));
            claims.forEach(j::claim);
        });
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
                        .with(jwtWithRealmRoles("GUEST")) // não permitido
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_should_allow_user_role() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .with(jwtWithRolesAndClaims(
                                List.of("USER"),
                                Map.of(
                                        "email", "user@ifrn.edu.br",
                                        "name", "Usuário Teste"
                                )
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
                                Map.of(
                                        "preferred_username", "valeria",
                                        "given_name", "Valéria"
                                )
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
