package br.edu.ifrn.eventsapi.cultural_events_api.dto.response;

public record VenueResponse(
        Long id,
        String name,
        String address,
        Integer capacity
) {}
