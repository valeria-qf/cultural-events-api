package br.edu.ifrn.eventsapi.cultural_events_api.integration;

import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.EventCreateRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Role;
import br.edu.ifrn.eventsapi.cultural_events_api.model.User;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.EventRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.ReservationRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.SessionRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.UserRepository;
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

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerIT extends IntegrationTestBase {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired EventRepository eventRepository;
    @Autowired SessionRepository sessionRepository;
    @Autowired ReservationRepository reservationRepository;

    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        sessionRepository.deleteAll();
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
    void create_shouldReturn403_withoutToken() throws Exception {
        mvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq())))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_shouldReturn403_forUserRole() throws Exception {
        String auth = bearer(Role.USER);

        mvc.perform(post("/api/v1/events")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq())))
                .andExpect(status().isForbidden());
    }

    @Test
    void crud_events_withAdmin() throws Exception {
        String auth = bearer(Role.ADMIN);

        String createdJson = mvc.perform(post("/api/v1/events")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title").value("Festival de Música"))
                .andExpect(jsonPath("$.description").value("Show e atrações"))
                .andExpect(jsonPath("$.category").value("Música"))
                .andExpect(jsonPath("$.startDate").value("2026-02-10"))
                .andExpect(jsonPath("$.endDate").value("2026-02-11"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(createdJson).get("id").asLong();

        mvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id));

        mvc.perform(get("/api/v1/events/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Festival de Música"));

        var updateReq = new EventCreateRequest(
                "Festival Atualizado",
                "Descrição nova",
                "Cultura",
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 2)
        );

        mvc.perform(put("/api/v1/events/{id}", id)
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Festival Atualizado"))
                .andExpect(jsonPath("$.description").value("Descrição nova"))
                .andExpect(jsonPath("$.category").value("Cultura"))
                .andExpect(jsonPath("$.startDate").value("2026-03-01"))
                .andExpect(jsonPath("$.endDate").value("2026-03-02"));

        mvc.perform(delete("/api/v1/events/{id}", id)
                        .header("Authorization", auth))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/events/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn404_whenEventNotFound() throws Exception {
        String auth = bearer(Role.ADMIN);

        var updateReq = new EventCreateRequest(
                "Qualquer",
                null,
                null,
                LocalDate.of(2026, 2, 10),
                LocalDate.of(2026, 2, 11)
        );

        mvc.perform(put("/api/v1/events/{id}", 999L)
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail", containsString("Event not found: 999")));
    }
}
