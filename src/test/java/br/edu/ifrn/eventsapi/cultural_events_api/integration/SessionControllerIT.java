package br.edu.ifrn.eventsapi.cultural_events_api.integration.controller;

import br.edu.ifrn.eventsapi.cultural_events_api.model.Event;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Session;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Venue;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.EventRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.SessionRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.VenueRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.support.IntegrationTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
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

class SessionControllerIT extends IntegrationTestBase {

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

    private String createBody(Long eventId, Long venueId) throws Exception {
        Map<String, Object> body = Map.of(
                "eventId", eventId,
                "venueId", venueId,
                "startsAt", LocalDateTime.now().plusDays(1).withSecond(0).withNano(0).toString(),
                "price", "25.00"
        );
        return objectMapper.writeValueAsString(body);
    }

    @Test
    void list_should_be_public() throws Exception {
        mockMvc.perform(get("/api/v1/sessions"))
                .andExpect(status().isOk());
    }

    @Test
    void list_by_event_should_be_public() throws Exception {
        var ev = seedEvent();
        mockMvc.perform(get("/api/v1/sessions").param("eventId", ev.getId().toString()))
                .andExpect(status().isOk());
    }

    @Test
    void get_should_be_public_200_or_404() throws Exception {
        var res = mockMvc.perform(get("/api/v1/sessions/999999")).andReturn();
        int sc = res.getResponse().getStatus();
        Assertions.assertTrue(sc == 200 || sc == 404);
    }

    @Test
    void create_should_return_401_without_token() throws Exception {
        var ev = seedEvent();
        var v = seedVenue();

        mockMvc.perform(post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(ev.getId(), v.getId())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_should_return_403_for_user_role() throws Exception {
        var ev = seedEvent();
        var v = seedVenue();

        mockMvc.perform(post("/api/v1/sessions")
                        .with(jwtWithRealmRoles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(ev.getId(), v.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_should_allow_admin() throws Exception {
        var ev = seedEvent();
        var v = seedVenue();

        mockMvc.perform(post("/api/v1/sessions")
                        .with(jwtWithRealmRoles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(ev.getId(), v.getId())))
                .andExpect(status().isCreated());
    }

    @Test
    void create_should_allow_organizer() throws Exception {
        var ev = seedEvent();
        var v = seedVenue();

        mockMvc.perform(post("/api/v1/sessions")
                        .with(jwtWithRealmRoles("ORGANIZER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(ev.getId(), v.getId())))
                .andExpect(status().isCreated());
    }

    @Test
    void update_should_require_admin_or_organizer() throws Exception {
        var ev = seedEvent();
        var v = seedVenue();

        Session s = sessionRepository.save(Session.builder()
                .event(ev)
                .venue(v)
                .startsAt(LocalDateTime.now().plusDays(3))
                .price(new BigDecimal("10.00"))
                .build());

        mockMvc.perform(put("/api/v1/sessions/" + s.getId())
                        .with(jwtWithRealmRoles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(ev.getId(), v.getId())))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/sessions/" + s.getId())
                        .with(jwtWithRealmRoles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(ev.getId(), v.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/sessions/" + s.getId())
                        .with(jwtWithRealmRoles("ORGANIZER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(ev.getId(), v.getId())))
                .andExpect(status().isOk());
    }

    @Test
    void delete_should_require_admin_or_organizer() throws Exception {
        var ev = seedEvent();
        var v = seedVenue();

        Session s = sessionRepository.save(Session.builder()
                .event(ev)
                .venue(v)
                .startsAt(LocalDateTime.now().plusDays(3))
                .price(new BigDecimal("10.00"))
                .build());

        mockMvc.perform(delete("/api/v1/sessions/" + s.getId())
                        .with(jwtWithRealmRoles("USER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/sessions/" + s.getId())
                        .with(jwtWithRealmRoles("ADMIN")))
                .andExpect(status().isNoContent());

        Session s2 = sessionRepository.save(Session.builder()
                .event(ev)
                .venue(v)
                .startsAt(LocalDateTime.now().plusDays(4))
                .price(new BigDecimal("11.00"))
                .build());

        mockMvc.perform(delete("/api/v1/sessions/" + s2.getId())
                        .with(jwtWithRealmRoles("ORGANIZER")))
                .andExpect(status().isNoContent());
    }
}
