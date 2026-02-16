package br.edu.ifrn.eventsapi.cultural_events_api.controller;

import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.ReservationCreateRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.dto.response.AvailabilityResponse;
import br.edu.ifrn.eventsapi.cultural_events_api.dto.response.ReservationResponse;
import br.edu.ifrn.eventsapi.cultural_events_api.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@Valid @RequestBody ReservationCreateRequest req,
                                      @AuthenticationPrincipal Jwt jwt) {

        String email = firstNonBlank(
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("preferred_username")
        );

        String name = firstNonBlank(
                jwt.getClaimAsString("name"),
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("preferred_username"),
                email
        );

        return reservationService.create(req, name, email);
    }

    @GetMapping
    public List<ReservationResponse> list(@RequestParam(required = false) String email) {
        return reservationService.list(email);
    }

    @GetMapping("/{id}")
    public ReservationResponse get(@PathVariable Long id) {
        return reservationService.get(id);
    }

    @PostMapping("/{id}/cancel")
    public ReservationResponse cancel(@PathVariable Long id) {
        return reservationService.cancel(id);
    }

    @GetMapping("/ticket/{code}")
    public ReservationResponse ticket(@PathVariable UUID code) {
        return reservationService.ticket(code);
    }

    @GetMapping("/availability/{sessionId}")
    public AvailabilityResponse availability(@PathVariable Long sessionId) {
        return reservationService.availability(sessionId);
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}
