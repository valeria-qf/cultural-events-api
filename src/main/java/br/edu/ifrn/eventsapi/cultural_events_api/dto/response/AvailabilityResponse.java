package br.edu.ifrn.eventsapi.cultural_events_api.dto.response;

public record AvailabilityResponse(
        Long sessionId,
        Integer capacity,
        Long reservedActive,
        Long available
) {}
