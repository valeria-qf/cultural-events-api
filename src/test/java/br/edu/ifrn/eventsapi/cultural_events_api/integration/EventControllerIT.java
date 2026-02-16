package br.edu.ifrn.eventsapi.cultural_events_api.integration.controller;

import br.edu.ifrn.eventsapi.cultural_events_api.model.Event;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.EventRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.support.IntegrationTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EventControllerIT extends IntegrationTestBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired EventRepository eventRepository;

    private String createBody() throws Exception {
        Map<String, Object> body = Map.of(
                "title", "Evento Teste",
                "description", "desc",
                "category", "Tech",
                "startDate", LocalDate.now().plusDays(1).toString(),
                "endDate", LocalDate.now().plusDays(2).toString()
        );
        return objectMapper.writeValueAsString(body);
    }

    private String updateBody() throws Exception {
        Map<String, Object> body = Map.of(
                "title", "Evento Atualizado",
                "description", "desc update",
                "category", "Edu",
                "startDate", LocalDate.now().plusDays(10).toString(),
                "endDate", LocalDate.now().plusDays(11).toString()
        );
        return objectMapper.writeValueAsString(body);
    }

    private Event seedEvent() {
        return eventRepository.save(Event.builder()
                .title("Seed Event")
                .description("seed desc")
                .category("Seed")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .build());
    }

    private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtWithRealmRoles(String... roles) {
        Collection<GrantedAuthority> authorities = Stream.of(roles)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return jwt()
                .jwt(j -> j.claim("realm_access", Map.of("roles", List.of(roles))))
                .authorities(authorities);
    }

    @Test
    void list_should_be_public() throws Exception {
        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk());
    }

    @Test
    void get_should_be_public_and_return_200_for_existing() throws Exception {
        var e = seedEvent();

        mockMvc.perform(get("/api/v1/events/" + e.getId()))
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

    @Test
    void update_should_return_401_without_token() throws Exception {
        var e = seedEvent();

        mockMvc.perform(put("/api/v1/events/" + e.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void update_should_return_403_for_user_role() throws Exception {
        var e = seedEvent();

        mockMvc.perform(put("/api/v1/events/" + e.getId())
                        .with(jwtWithRealmRoles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_should_allow_admin() throws Exception {
        var e = seedEvent();

        mockMvc.perform(put("/api/v1/events/" + e.getId())
                        .with(jwtWithRealmRoles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody()))
                .andExpect(status().isOk());
    }

    @Test
    void delete_should_return_401_without_token() throws Exception {
        var e = seedEvent();

        mockMvc.perform(delete("/api/v1/events/" + e.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_should_return_403_for_user_role() throws Exception {
        var e = seedEvent();

        mockMvc.perform(delete("/api/v1/events/" + e.getId())
                        .with(jwtWithRealmRoles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_should_allow_admin() throws Exception {
        var e = seedEvent();

        mockMvc.perform(delete("/api/v1/events/" + e.getId())
                        .with(jwtWithRealmRoles("ADMIN")))
                .andExpect(status().isNoContent());
    }
}
