package br.edu.ifrn.eventsapi.cultural_events_api.dto.request;

import br.edu.ifrn.eventsapi.cultural_events_api.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Size(min = 6, max = 60) String password,
        Role role
) {}
