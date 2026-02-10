package br.edu.ifrn.eventsapi.cultural_events_api.dto.response;

import br.edu.ifrn.eventsapi.cultural_events_api.model.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationResponse(
        Long id,
        Long sessionId,
        String customerName,
        String customerEmail,
        Integer quantity,
        ReservationStatus status,
        UUID code,
        LocalDateTime createdAt
) {}
