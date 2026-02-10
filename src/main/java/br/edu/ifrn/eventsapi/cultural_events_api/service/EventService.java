package br.edu.ifrn.eventsapi.cultural_events_api.service;

import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.EventCreateRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.dto.response.EventResponse;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Event;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public EventResponse create(EventCreateRequest req) {
        Event e = Event.builder()
                .title(req.title())
                .description(req.description())
                .category(req.category())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .build();
        e = eventRepository.save(e);
        return toResponse(e);
    }

    public List<EventResponse> list() {
        return eventRepository.findAll().stream().map(this::toResponse).toList();
    }

    public EventResponse get(Long id) {
        Event e = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found: " + id));
        return toResponse(e);
    }

    public EventResponse update(Long id, EventCreateRequest req) {
        Event e = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found: " + id));

        e.setTitle(req.title());
        e.setDescription(req.description());
        e.setCategory(req.category());
        e.setStartDate(req.startDate());
        e.setEndDate(req.endDate());

        e = eventRepository.save(e);
        return toResponse(e);
    }

    public void delete(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EntityNotFoundException("Event not found: " + id);
        }
        eventRepository.deleteById(id);
    }

    public Event findEntity(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found: " + id));
    }

    private EventResponse toResponse(Event e) {
        return new EventResponse(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getCategory(),
                e.getStartDate(),
                e.getEndDate()
        );
    }
}
