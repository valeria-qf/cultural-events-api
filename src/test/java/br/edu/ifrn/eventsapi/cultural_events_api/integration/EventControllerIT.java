package br.edu.ifrn.eventsapi.cultural_events_api.integration;

import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.EventCreateRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.EventRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.ReservationRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.SessionRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.support.IntegrationTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EventControllerIT extends IntegrationTestBase {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired EventRepository eventRepository;
    @Autowired SessionRepository sessionRepository;
    @Autowired ReservationRepository reservationRepository;

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        sessionRepository.deleteAll();
        eventRepository.deleteAll();
    }

    private EventCreateRequest createReq() {
        return new EventCreateRequest(
                "Festival de Música",
                "Show e atrações",
                "Música",
                LocalDate.of(2026, 2, 10),
                LocalDate.of(2026, 2, 11)
        );
    }

    @Test
    void crud_events() throws Exception {

        String createdJson = mvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(createdJson).get("id").asLong();

        mvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(get("/api/v1/events/{id}", id))
                .andExpect(status().isOk());

        var updateReq = new EventCreateRequest(
                "Festival Atualizado",
                "Descrição nova",
                "Cultura",
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 2)
        );

        mvc.perform(put("/api/v1/events/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk());

        mvc.perform(delete("/api/v1/events/{id}", id))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/events/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn404_whenEventNotFound() throws Exception {

        var updateReq = new EventCreateRequest(
                "Qualquer",
                null,
                null,
                LocalDate.of(2026, 2, 10),
                LocalDate.of(2026, 2, 11)
        );

        mvc.perform(put("/api/v1/events/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail", containsString("Event not found: 999")));
    }
}
