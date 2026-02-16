package br.edu.ifrn.eventsapi.cultural_events_api.integration.controller;

import br.edu.ifrn.eventsapi.cultural_events_api.support.IntegrationTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EventControllerIT extends IntegrationTestBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String createBody() throws Exception {

        var body = Map.of(
                "title", "Evento Teste",
                "description", "desc",
                "category", "Tech",
                "startDate", LocalDate.now().plusDays(1).toString(),
                "endDate", LocalDate.now().plusDays(2).toString()
        );
        return objectMapper.writeValueAsString(body);
    }

    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor
    jwtWithRealmRoles(String... roles) {
        return jwt().jwt(j -> j.claim("realm_access", Map.of("roles", List.of(roles))));
    }

    @Test
    void list_should_be_public() throws Exception {
        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk());
    }

    @Test
    void create_should_return_401_without_token() throws Exception {
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_should_return_403_for_user_role() throws Exception {
        mockMvc.perform(post("/api/v1/events")
                        .with(jwtWithRealmRoles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_should_allow_admin() throws Exception {
        mockMvc.perform(post("/api/v1/events")
                        .with(jwtWithRealmRoles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isCreated());
    }

    @Test
    void create_should_allow_organizer() throws Exception {
        mockMvc.perform(post("/api/v1/events")
                        .with(jwtWithRealmRoles("ORGANIZER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isCreated());
    }
}
