package br.edu.ifrn.eventsapi.cultural_events_api.service;

import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.VenueCreateRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.dto.response.VenueResponse;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Venue;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.VenueRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueResponse create(VenueCreateRequest req) {
        Venue v = Venue.builder()
                .name(req.name())
                .address(req.address())
                .capacity(req.capacity())
                .build();
        v = venueRepository.save(v);
        return toResponse(v);
    }

    public List<VenueResponse> list() {
        return venueRepository.findAll().stream().map(this::toResponse).toList();
    }

    public VenueResponse get(Long id) {
        Venue v = venueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Venue not found: " + id));
        return toResponse(v);
    }

    public VenueResponse update(Long id, VenueCreateRequest req) {
        Venue v = venueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Venue not found: " + id));
        v.setName(req.name());
        v.setAddress(req.address());
        v.setCapacity(req.capacity());
        v = venueRepository.save(v);
        return toResponse(v);
    }

    public void delete(Long id) {
        if (!venueRepository.existsById(id)) {
            throw new EntityNotFoundException("Venue not found: " + id);
        }
        venueRepository.deleteById(id);
    }

    private VenueResponse toResponse(Venue v) {
        return new VenueResponse(v.getId(), v.getName(), v.getAddress(), v.getCapacity());
    }
}
