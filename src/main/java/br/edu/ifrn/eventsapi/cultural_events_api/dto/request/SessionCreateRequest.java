package br.edu.ifrn.eventsapi.cultural_events_api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SessionCreateRequest(
        @NotNull Long eventId,
        @NotNull Long venueId,
        @NotNull LocalDateTime startsAt,
        @NotNull @DecimalMin("0.00") BigDecimal price
) {}
