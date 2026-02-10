package br.edu.ifrn.eventsapi.cultural_events_api.controller;

import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.ReservationCreateRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.dto.response.AvailabilityResponse;
import br.edu.ifrn.eventsapi.cultural_events_api.dto.response.ReservationResponse;
import br.edu.ifrn.eventsapi.cultural_events_api.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ReservationResponse create(@Valid @RequestBody ReservationCreateRequest req) {
        return reservationService.create(req);
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
}
