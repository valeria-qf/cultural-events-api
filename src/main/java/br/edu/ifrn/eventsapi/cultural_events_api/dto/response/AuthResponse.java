package br.edu.ifrn.eventsapi.cultural_events_api.dto.response;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String email,
        String role
) {}
