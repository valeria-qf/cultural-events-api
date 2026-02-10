package br.edu.ifrn.eventsapi.cultural_events_api.integration;

import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.SessionCreateRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Event;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Role;
import br.edu.ifrn.eventsapi.cultural_events_api.model.User;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Venue;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.EventRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.ReservationRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.SessionRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.UserRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.VenueRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.service.JwtService;
import br.edu.ifrn.eventsapi.cultural_events_api.support.IntegrationTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SessionControllerIT extends IntegrationTestBase {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired SessionRepository sessionRepository;
    @Autowired EventRepository eventRepository;
    @Autowired VenueRepository venueRepository;
    @Autowired ReservationRepository reservationRepository;

    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        sessionRepository.deleteAll();
        venueRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String bearer(Role role) {
        String email = role.name().toLowerCase() + "@ifrn.edu.br";
        userRepository.save(User.builder()
                .name(role.name())
                .email(email)
                .passwordHash(passwordEncoder.encode("12345678"))
                .role(role)
                .build());
        return "Bearer " + jwtService.generateToken(email, Map.of());
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
    void create_shouldReturn403_withoutToken() throws Exception {
        Event e = seedEvent();
        Venue v = seedVenue(100);

        var req = new SessionCreateRequest(
                e.getId(),
                v.getId(),
                LocalDateTime.of(2026, 2, 10, 19, 0),
                BigDecimal.valueOf(50)
        );

        mvc.perform(post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_shouldReturn403_forUserRole() throws Exception {
        String auth = bearer(Role.USER);
        Event e = seedEvent();
        Venue v = seedVenue(100);

        var req = new SessionCreateRequest(
                e.getId(),
                v.getId(),
                LocalDateTime.of(2026, 2, 10, 19, 0),
                BigDecimal.valueOf(50)
        );

        mvc.perform(post("/api/v1/sessions")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void crud_sessions_withAdmin_and_listByEvent_public() throws Exception {
        String auth = bearer(Role.ADMIN);
        Event e = seedEvent();
        Venue v = seedVenue(100);

        var createReq = new SessionCreateRequest(
                e.getId(),
                v.getId(),
                LocalDateTime.of(2026, 2, 10, 19, 0),
                BigDecimal.valueOf(50)
        );

        String createdJson = mvc.perform(post("/api/v1/sessions")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.eventId").value(e.getId()))
                .andExpect(jsonPath("$.venueId").value(v.getId()))
                .andExpect(jsonPath("$.startsAt", not(isEmptyOrNullString())))
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
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId))
                .andExpect(jsonPath("$.price").value(80));

        mvc.perform(delete("/api/v1/sessions/{id}", sessionId)
                        .header("Authorization", auth))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/sessions/{id}", sessionId))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn404_whenVenueNotFound() throws Exception {
        String auth = bearer(Role.ADMIN);
        Event e = seedEvent();

        var req = new SessionCreateRequest(
                e.getId(),
                999L,
                LocalDateTime.of(2026, 2, 10, 19, 0),
                BigDecimal.valueOf(50)
        );

        mvc.perform(post("/api/v1/sessions")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail", containsString("Venue not found: 999")));
    }

    @Test
    void update_shouldReturn404_whenSessionNotFound() throws Exception {
        String auth = bearer(Role.ADMIN);
        Event e = seedEvent();
        Venue v = seedVenue(100);

        var req = new SessionCreateRequest(
                e.getId(),
                v.getId(),
                LocalDateTime.of(2026, 2, 10, 19, 0),
                BigDecimal.valueOf(50)
        );

        mvc.perform(put("/api/v1/sessions/{id}", 999L)
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail", containsString("Session not found: 999")));
    }
}
