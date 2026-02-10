package br.edu.ifrn.eventsapi.cultural_events_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EventCreateRequest(
        @NotBlank String title,
        String description,
        String category,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {}
