package br.edu.ifrn.eventsapi.cultural_events_api.integration.controller;

import br.edu.ifrn.eventsapi.cultural_events_api.model.Event;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Session;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Venue;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.EventRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.SessionRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.VenueRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.support.IntegrationTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReservationControllerIT extends IntegrationTestBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired EventRepository eventRepository;
    @Autowired VenueRepository venueRepository;
    @Autowired SessionRepository sessionRepository;

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

    private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor
    jwtWithRolesAndClaims(List<String> roles, Map<String, Object> claims) {

        Collection<GrantedAuthority> authorities = roles.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return jwt()
                .jwt(j -> {
                    j.claim("realm_access", Map.of("roles", roles));
                    claims.forEach(j::claim);
                })
                .authorities(authorities);
    }

    private Event seedEvent() {
        return eventRepository.save(Event.builder()
                .title("Evento Teste")
                .description("desc")
                .category("Tech")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .build());
    }

    private Venue seedVenue() {
        return venueRepository.save(Venue.builder()
                .name("Auditório Central")
                .address("Rua Exemplo, 123")
                .capacity(200)
                .build());
    }

    private Session seedSession(Event ev, Venue v) {
        return sessionRepository.save(Session.builder()
                .event(ev)
                .venue(v)
                .startsAt(LocalDateTime.now().plusDays(1).withSecond(0).withNano(0))
                .price(new BigDecimal("25.00"))
                .build());
    }

    private String createBody(Long sessionId, String customerName, String customerEmail) throws Exception {
        Map<String, Object> body = Map.of(
                "sessionId", sessionId,
                "customerName", customerName,
                "customerEmail", customerEmail,
                "quantity", 1
        );
        return objectMapper.writeValueAsString(body);
    }

    @Test
    void create_should_return_401_without_token() throws Exception {
        var ev = seedEvent();
        var v = seedVenue();
        var s = seedSession(ev, v);

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(s.getId(), "Usuário Teste", "user@ifrn.edu.br")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_should_return_403_when_role_not_allowed() throws Exception {
        var ev = seedEvent();
        var v = seedVenue();
        var s = seedSession(ev, v);

        mockMvc.perform(post("/api/v1/reservations")
                        .with(jwtWithRealmRoles("GUEST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(s.getId(), "Usuário Teste", "user@ifrn.edu.br")))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_should_allow_user_role() throws Exception {
        var ev = seedEvent();
        var v = seedVenue();
        var s = seedSession(ev, v);

        mockMvc.perform(post("/api/v1/reservations")
                        .with(jwtWithRolesAndClaims(
                                List.of("USER"),
                                Map.of("email", "user@ifrn.edu.br", "name", "Usuário Teste")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(s.getId(), "Cliente Body", "cliente@x.com")))
                .andExpect(status().isCreated());
    }

    @Test
    void create_should_fallback_to_preferred_username_when_email_missing() throws Exception {
        var ev = seedEvent();
        var v = seedVenue();
        var s = seedSession(ev, v);

        mockMvc.perform(post("/api/v1/reservations")
                        .with(jwtWithRolesAndClaims(
                                List.of("USER"),
                                Map.of("preferred_username", "valeria", "given_name", "Valéria")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(s.getId(), "Cliente Body", "cliente@x.com")))
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
    void get_cancel_ticket_availability_should_require_auth() throws Exception {
        mockMvc.perform(get("/api/v1/reservations/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/reservations/1/cancel"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/reservations/ticket/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/reservations/availability/1"))
                .andExpect(status().isUnauthorized());
    }
}
