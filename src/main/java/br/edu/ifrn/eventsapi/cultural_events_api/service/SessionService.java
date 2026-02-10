package br.edu.ifrn.eventsapi.cultural_events_api.service;

import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.SessionCreateRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.dto.response.SessionResponse;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Event;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Session;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Venue;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.SessionRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.VenueRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final EventService eventService;
    private final VenueRepository venueRepository;

    @CacheEvict(cacheNames = {"sessions_list", "sessions_by_id", "sessions_by_event"}, allEntries = true)
    public SessionResponse create(SessionCreateRequest req) {
        Event event = eventService.findEntity(req.eventId());
        Venue venue = venueRepository.findById(req.venueId())
                .orElseThrow(() -> new EntityNotFoundException("Venue not found: " + req.venueId()));

        Session s = new Session();
        s.setEvent(event);
        s.setVenue(venue);
        s.setStartsAt(req.startsAt());
        s.setPrice(req.price());

        s = sessionRepository.save(s);
        return toResponse(s);
    }

    @Cacheable(cacheNames = "sessions_list")
    public List<SessionResponse> list() {
        return sessionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Cacheable(cacheNames = "sessions_by_id", key = "#id")
    public SessionResponse get(Long id) {
        return toResponse(findEntity(id));
    }

    @Cacheable(cacheNames = "sessions_by_event", key = "#eventId")
    public List<SessionResponse> listByEvent(Long eventId) {
        return sessionRepository.findByEventId(eventId).stream().map(this::toResponse).toList();
    }

    @CacheEvict(cacheNames = {"sessions_list", "sessions_by_id", "sessions_by_event"}, allEntries = true)
    public SessionResponse update(Long id, SessionCreateRequest req) {
        Session s = findEntity(id);

        Event event = eventService.findEntity(req.eventId());
        Venue venue = venueRepository.findById(req.venueId())
                .orElseThrow(() -> new EntityNotFoundException("Venue not found: " + req.venueId()));

        s.setEvent(event);
        s.setVenue(venue);
        s.setStartsAt(req.startsAt());
        s.setPrice(req.price());

        s = sessionRepository.save(s);
        return toResponse(s);
    }

    @CacheEvict(cacheNames = {"sessions_list", "sessions_by_id", "sessions_by_event"}, allEntries = true)
    public void delete(Long id) {
        if (!sessionRepository.existsById(id)) {
            throw new EntityNotFoundException("Session not found: " + id);
        }
        sessionRepository.deleteById(id);
    }

    public Session findEntity(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + id));
    }

    private SessionResponse toResponse(Session s) {
        return new SessionResponse(
                s.getId(),
                s.getEvent().getId(),
                s.getVenue().getId(),
                s.getStartsAt(),
                s.getPrice()
        );
    }
}
