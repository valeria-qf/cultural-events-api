package br.edu.ifrn.eventsapi.cultural_events_api.dto.response;

import java.time.LocalDate;

public record EventResponse(
        Long id,
        String title,
        String description,
        String category,
        LocalDate startDate,
        LocalDate endDate
) {}
