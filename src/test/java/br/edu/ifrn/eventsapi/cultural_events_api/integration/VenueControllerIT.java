package br.edu.ifrn.eventsapi.cultural_events_api.integration;

import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.VenueCreateRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VenueControllerIT extends IntegrationTestBase {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired VenueRepository venueRepository;
    @Autowired SessionRepository sessionRepository;
    @Autowired ReservationRepository reservationRepository;

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        sessionRepository.deleteAll();
        venueRepository.deleteAll();
    }

    private VenueCreateRequest createReq() {
        return new VenueCreateRequest(
                "Auditório Central",
                "IFRN - Campus",
                500
        );
    }

    @Test
    void crud_venues_and_gets_public() throws Exception {

        // CREATE
        String createdJson = mvc.perform(post("/api/v1/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("Auditório Central"))
                .andExpect(jsonPath("$.address").value("IFRN - Campus"))
                .andExpect(jsonPath("$.capacity").value(500))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(createdJson).get("id").asLong();

        // LIST (public)
        mvc.perform(get("/api/v1/venues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id));

        // GET BY ID (public)
        mvc.perform(get("/api/v1/venues/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Auditório Central"));

        // UPDATE
        var updateReq = new VenueCreateRequest(
                "Novo Nome",
                "Novo Endereço",
                700
        );

        mvc.perform(put("/api/v1/venues/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Novo Nome"))
                .andExpect(jsonPath("$.address").value("Novo Endereço"))
                .andExpect(jsonPath("$.capacity").value(700));

        // DELETE
        mvc.perform(delete("/api/v1/venues/{id}", id))
                .andExpect(status().isNoContent());

        // GET AFTER DELETE → 404
        mvc.perform(get("/api/v1/venues/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail", containsString("Venue not found: " + id)));
    }

    @Test
    void update_shouldReturn404_whenVenueNotFound() throws Exception {

        mvc.perform(put("/api/v1/venues/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail", containsString("Venue not found: 999")));
    }
}
