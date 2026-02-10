package br.edu.ifrn.eventsapi.cultural_events_api.integration;

import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.VenueCreateRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Role;
import br.edu.ifrn.eventsapi.cultural_events_api.model.User;
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

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VenueControllerIT extends IntegrationTestBase {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired VenueRepository venueRepository;
    @Autowired SessionRepository sessionRepository;
    @Autowired ReservationRepository reservationRepository;

    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        sessionRepository.deleteAll();
        venueRepository.deleteAll();
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

    private VenueCreateRequest createReq() {
        return new VenueCreateRequest("Auditório Central", "IFRN - Campus", 500);
    }

    @Test
    void create_shouldReturn403_withoutToken() throws Exception {
        mvc.perform(post("/api/v1/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq())))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_shouldReturn403_forUserRole() throws Exception {
        String auth = bearer(Role.USER);

        mvc.perform(post("/api/v1/venues")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq())))
                .andExpect(status().isForbidden());
    }

    @Test
    void crud_venues_withAdmin_and_gets_public() throws Exception {
        String auth = bearer(Role.ADMIN);

        String createdJson = mvc.perform(post("/api/v1/venues")
                        .header("Authorization", auth)
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

        mvc.perform(get("/api/v1/venues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id));

        mvc.perform(get("/api/v1/venues/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Auditório Central"));

        var updateReq = new VenueCreateRequest("Novo Nome", "Novo Endereço", 700);

        mvc.perform(put("/api/v1/venues/{id}", id)
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Novo Nome"))
                .andExpect(jsonPath("$.address").value("Novo Endereço"))
                .andExpect(jsonPath("$.capacity").value(700));

        mvc.perform(delete("/api/v1/venues/{id}", id)
                        .header("Authorization", auth))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/venues/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail", containsString("Venue not found: " + id)));
    }

    @Test
    void update_shouldReturn404_whenVenueNotFound() throws Exception {
        String auth = bearer(Role.ADMIN);

        mvc.perform(put("/api/v1/venues/{id}", 999L)
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail", containsString("Venue not found: 999")));
    }
}
