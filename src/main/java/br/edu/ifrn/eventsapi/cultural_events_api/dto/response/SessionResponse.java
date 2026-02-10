package br.edu.ifrn.eventsapi.cultural_events_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SessionResponse(
        Long id,
        Long eventId,
        Long venueId,
        LocalDateTime startsAt,
        BigDecimal price
) {}
