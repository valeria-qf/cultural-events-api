package br.edu.ifrn.eventsapi.cultural_events_api.controller;

import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.VenueCreateRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.dto.response.VenueResponse;
import br.edu.ifrn.eventsapi.cultural_events_api.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VenueResponse create(@Valid @RequestBody VenueCreateRequest req) {
        return venueService.create(req);
    }

    @GetMapping
    public List<VenueResponse> list() {
        return venueService.list();
    }

    @GetMapping("/{id}")
    public VenueResponse get(@PathVariable Long id) {
        return venueService.get(id);
    }

    @PutMapping("/{id}")
    public VenueResponse update(@PathVariable Long id, @Valid @RequestBody VenueCreateRequest req) {
        return venueService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        venueService.delete(id);
    }
}
