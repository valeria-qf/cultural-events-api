package br.edu.ifrn.eventsapi.cultural_events_api.integration;

import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.SessionCreateRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Event;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Venue;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.EventRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.ReservationRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.SessionRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.VenueRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.support.IntegrationTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SessionControllerIT extends IntegrationTestBase {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired SessionRepository sessionRepository;
    @Autowired EventRepository eventRepository;
    @Autowired VenueRepository venueRepository;
    @Autowired ReservationRepository reservationRepository;

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        sessionRepository.deleteAll();
        venueRepository.deleteAll();
        eventRepository.deleteAll();
    }

    private Event seedEvent() {
        return eventRepository.save(Event.builder()
                .title("Festival")
                .description("Desc")
                .category("Música")
                .startDate(LocalDate.of(2026, 2, 10))
                .endDate(LocalDate.of(2026, 2, 11))
                .build());
    }

    private Venue seedVenue(int capacity) {
        return venueRepository.save(Venue.builder()
                .name("Auditório Central")
                .address("IFRN")
                .capacity(capacity)
                .build());
    }

    @Test
    void crud_sessions_and_listByEvent() throws Exception {

        Event e = seedEvent();
        Venue v = seedVenue(100);

        var createReq = new SessionCreateRequest(
                e.getId(),
                v.getId(),
                LocalDateTime.of(2026, 2, 10, 19, 0),
                BigDecimal.valueOf(50)
        );

        String createdJson = mvc.perform(post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.eventId").value(e.getId()))
                .andExpect(jsonPath("$.venueId").value(v.getId()))
                .andExpect(jsonPath("$.price").value(50))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long sessionId = objectMapper.readTree(createdJson).get("id").asLong();

        mvc.perform(get("/api/v1/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(sessionId));

        mvc.perform(get("/api/v1/sessions")
                        .param("eventId", String.valueOf(e.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].eventId").value(e.getId()));

        mvc.perform(get("/api/v1/sessions/{id}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId));

        var updateReq = new SessionCreateRequest(
                e.getId(),
                v.getId(),
                LocalDateTime.of(2026, 3, 1, 20, 0),
                BigDecimal.valueOf(80)
        );

        mvc.perform(put("/api/v1/sessions/{id}", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId))
                .andExpect(jsonPath("$.price").value(80));

        mvc.perform(delete("/api/v1/sessions/{id}", sessionId))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/sessions/{id}", sessionId))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn404_whenVenueNotFound() throws Exception {

        Event e = seedEvent();

        var req = new SessionCreateRequest(
                e.getId(),
                999L,
                LocalDateTime.of(2026, 2, 10, 19, 0),
                BigDecimal.valueOf(50)
        );

        mvc.perform(post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail", containsString("Venue not found: 999")));
    }

    @Test
    void update_shouldReturn404_whenSessionNotFound() throws Exception {

        Event e = seedEvent();
        Venue v = seedVenue(100);

        var req = new SessionCreateRequest(
                e.getId(),
                v.getId(),
                LocalDateTime.of(2026, 2, 10, 19, 0),
                BigDecimal.valueOf(50)
        );

        mvc.perform(put("/api/v1/sessions/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail", containsString("Session not found: 999")));
    }
}
