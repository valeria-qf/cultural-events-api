package br.edu.ifrn.eventsapi.cultural_events_api.integration;

import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.ReservationCreateRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.model.*;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.*;
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
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReservationControllerIT extends IntegrationTestBase {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired ReservationRepository reservationRepository;
    @Autowired SessionRepository sessionRepository;
    @Autowired VenueRepository venueRepository;
    @Autowired EventRepository eventRepository;

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        sessionRepository.deleteAll();
        venueRepository.deleteAll();
        eventRepository.deleteAll();
    }

    private Session seedSession(int capacity) {
        Event e = eventRepository.save(Event.builder()
                .title("Festival")
                .description("Desc")
                .category("Música")
                .startDate(LocalDate.of(2026, 2, 10))
                .endDate(LocalDate.of(2026, 2, 11))
                .build());

        Venue v = venueRepository.save(Venue.builder()
                .name("Auditório Central")
                .address("IFRN")
                .capacity(capacity)
                .build());

        return sessionRepository.save(Session.builder()
                .event(e)
                .venue(v)
                .startsAt(LocalDateTime.of(2026, 2, 10, 19, 0))
                .price(BigDecimal.valueOf(50))
                .build());
    }

    @Test
    void create_get_list_ticket_cancel_availability_flow() throws Exception {

        Session s = seedSession(10);

        var createReq = new ReservationCreateRequest(
                s.getId(),
                "Cliente 1",
                "cliente@ifrn.edu.br",
                3
        );

        // CREATE
        String createdJson = mvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.sessionId").value(s.getId()))
                .andExpect(jsonPath("$.customerName").value("Cliente 1"))
                .andExpect(jsonPath("$.customerEmail").value("cliente@ifrn.edu.br"))
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.status").value(ReservationStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.code", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.createdAt", not(isEmptyOrNullString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long reservationId = objectMapper.readTree(createdJson).get("id").asLong();
        UUID code = UUID.fromString(objectMapper.readTree(createdJson).get("code").asText());

        // GET BY ID
        mvc.perform(get("/api/v1/reservations/{id}", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId))
                .andExpect(jsonPath("$.status").value(ReservationStatus.ACTIVE.name()));

        // LIST BY EMAIL
        mvc.perform(get("/api/v1/reservations")
                        .param("email", "cliente@ifrn.edu.br"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(reservationId))
                .andExpect(jsonPath("$[0].status").value(ReservationStatus.ACTIVE.name()));

        // GET TICKET BY CODE
        mvc.perform(get("/api/v1/reservations/ticket/{code}", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId))
                .andExpect(jsonPath("$.code").value(code.toString()));

        // AVAILABILITY
        mvc.perform(get("/api/v1/reservations/availability/{sessionId}", s.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(s.getId()))
                .andExpect(jsonPath("$.capacity").value(10))
                .andExpect(jsonPath("$.reservedActive").value(3))
                .andExpect(jsonPath("$.available").value(7));

        // CANCEL
        mvc.perform(post("/api/v1/reservations/{id}/cancel", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId))
                .andExpect(jsonPath("$.status").value(ReservationStatus.CANCELED.name()));

        // AVAILABILITY AFTER CANCEL
        mvc.perform(get("/api/v1/reservations/availability/{sessionId}", s.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservedActive").value(0))
                .andExpect(jsonPath("$.available").value(10));
    }

    @Test
    void create_shouldReturn400_whenNotEnoughSeats() throws Exception {

        Session s = seedSession(5);

        var createReq = new ReservationCreateRequest(
                s.getId(),
                "Cliente 1",
                "cliente@ifrn.edu.br",
                10
        );

        mvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail", containsString("Not enough seats. Available:")));
    }
}
