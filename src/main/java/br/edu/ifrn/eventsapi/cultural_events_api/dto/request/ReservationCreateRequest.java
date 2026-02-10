package br.edu.ifrn.eventsapi.cultural_events_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReservationCreateRequest(
        @NotNull Long sessionId,
        @NotBlank String customerName,
        @NotBlank @Email String customerEmail,
        @NotNull @Min(1) Integer quantity
) {}
