package br.edu.ifrn.eventsapi.cultural_events_api.integration.controller;

import br.edu.ifrn.eventsapi.cultural_events_api.support.IntegrationTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VenueControllerIT extends IntegrationTestBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String createBody() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Auditório Central",
                "address", "Rua Exemplo, 123 - Natal/RN",
                "capacity", 200
        );
        return objectMapper.writeValueAsString(body);
    }

    private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor
    jwtWithRealmRoles(String... roles) {

        List<SimpleGrantedAuthority> authorities = Stream.of(roles)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .toList();

        return jwt()
                .jwt(j -> j.claim("realm_access", Map.of("roles", List.of(roles))))
                .authorities(authorities);
    }

    @Test
    void list_should_be_public() throws Exception {
        mockMvc.perform(get("/api/v1/venues"))
                .andExpect(status().isOk());
    }

    @Test
    void get_should_be_public() throws Exception {
        mockMvc.perform(get("/api/v1/venues/999999"))
                .andExpect(status().isNotEqualTo(401))
                .andExpect(status().isNotEqualTo(403));
    }

    @Test
    void create_should_return_401_without_token() throws Exception {
        mockMvc.perform(post("/api/v1/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_should_return_403_for_user_role() throws Exception {
        mockMvc.perform(post("/api/v1/venues")
                        .with(jwtWithRealmRoles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_should_allow_admin() throws Exception {
        mockMvc.perform(post("/api/v1/venues")
                        .with(jwtWithRealmRoles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isCreated());
    }

    @Test
    void create_should_allow_organizer() throws Exception {
        mockMvc.perform(post("/api/v1/venues")
                        .with(jwtWithRealmRoles("ORGANIZER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isCreated());
    }

    @Test
    void update_should_return_401_without_token() throws Exception {
        mockMvc.perform(put("/api/v1/venues/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void update_should_return_403_for_user_role() throws Exception {
        mockMvc.perform(put("/api/v1/venues/1")
                        .with(jwtWithRealmRoles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_should_allow_admin() throws Exception {
        mockMvc.perform(put("/api/v1/venues/1")
                        .with(jwtWithRealmRoles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isNotEqualTo(401))
                .andExpect(status().isNotEqualTo(403));
    }

    @Test
    void delete_should_return_401_without_token() throws Exception {
        mockMvc.perform(delete("/api/v1/venues/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_should_return_403_for_user_role() throws Exception {
        mockMvc.perform(delete("/api/v1/venues/1")
                        .with(jwtWithRealmRoles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_should_allow_admin() throws Exception {
        mockMvc.perform(delete("/api/v1/venues/1")
                        .with(jwtWithRealmRoles("ADMIN")))
                .andExpect(status().isNotEqualTo(401))
                .andExpect(status().isNotEqualTo(403));
    }
}
