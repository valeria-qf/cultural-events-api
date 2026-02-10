package br.edu.ifrn.eventsapi.cultural_events_api.service;

import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.SessionCreateRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.dto.response.SessionResponse;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Event;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Session;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Venue;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.SessionRepository;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.VenueRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    SessionRepository sessionRepository;

    @Mock
    EventService eventService;

    @Mock
    VenueRepository venueRepository;

    @InjectMocks
    SessionService service;

    private SessionCreateRequest req(Long eventId, Long venueId) {
        return new SessionCreateRequest(
                eventId,
                venueId,
                LocalDateTime.of(2026, 2, 10, 19, 0),
                BigDecimal.valueOf(50)
        );
    }

    private Event event(Long id) {
        return Event.builder().id(id).title("Evento").startDate(null).endDate(null).build();
    }

    private Venue venue(Long id) {
        return Venue.builder().id(id).name("Local").address("Endereço").capacity(100).build();
    }

    private Session session(Long id, Event e, Venue v) {
        return Session.builder()
                .id(id)
                .event(e)
                .venue(v)
                .startsAt(LocalDateTime.of(2026, 2, 10, 19, 0))
                .price(BigDecimal.valueOf(50))
                .build();
    }

    @Test
    @DisplayName("create deve salvar e retornar SessionResponse")
    void create_ok() {
        Long eventId = 1L;
        Long venueId = 2L;

        Event e = event(eventId);
        Venue v = venue(venueId);

        when(eventService.findEntity(eventId)).thenReturn(e);
        when(venueRepository.findById(venueId)).thenReturn(Optional.of(v));
        when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> {
            Session s = inv.getArgument(0);
            s.setId(10L);
            return s;
        });

        SessionResponse res = service.create(req(eventId, venueId));

        assertEquals(10L, res.id());
        assertEquals(eventId, res.eventId());
        assertEquals(venueId, res.venueId());
        assertEquals(LocalDateTime.of(2026, 2, 10, 19, 0), res.startsAt());
        assertEquals(BigDecimal.valueOf(50), res.price());

        verify(eventService).findEntity(eventId);
        verify(venueRepository).findById(venueId);
        verify(sessionRepository).save(any(Session.class));
        verifyNoMoreInteractions(eventService, venueRepository, sessionRepository);
    }

    @Test
    @DisplayName("create deve lançar EntityNotFoundException quando venue não existe")
    void create_venueNotFound() {
        Long eventId = 1L;
        Long venueId = 999L;

        when(eventService.findEntity(eventId)).thenReturn(event(eventId));
        when(venueRepository.findById(venueId)).thenReturn(Optional.empty());

        var ex = assertThrows(EntityNotFoundException.class, () -> service.create(req(eventId, venueId)));
        assertEquals("Venue not found: " + venueId, ex.getMessage());

        verify(eventService).findEntity(eventId);
        verify(venueRepository).findById(venueId);
        verify(sessionRepository, never()).save(any());
        verifyNoMoreInteractions(eventService, venueRepository, sessionRepository);
    }

    @Test
    @DisplayName("list deve retornar lista de SessionResponse")
    void list_ok() {
        Event e = event(1L);
        Venue v = venue(2L);

        when(sessionRepository.findAll()).thenReturn(List.of(
                session(10L, e, v),
                session(11L, e, v)
        ));

        List<SessionResponse> res = service.list();

        assertEquals(2, res.size());
        assertEquals(10L, res.get(0).id());
        assertEquals(11L, res.get(1).id());

        verify(sessionRepository).findAll();
        verifyNoMoreInteractions(sessionRepository);
        verifyNoInteractions(eventService, venueRepository);
    }

    @Test
    @DisplayName("get deve retornar SessionResponse quando existe")
    void get_ok() {
        Long id = 10L;
        Event e = event(1L);
        Venue v = venue(2L);

        when(sessionRepository.findById(id)).thenReturn(Optional.of(session(id, e, v)));

        SessionResponse res = service.get(id);

        assertEquals(id, res.id());
        assertEquals(1L, res.eventId());
        assertEquals(2L, res.venueId());

        verify(sessionRepository).findById(id);
        verifyNoMoreInteractions(sessionRepository);
        verifyNoInteractions(eventService, venueRepository);
    }

    @Test
    @DisplayName("get deve lançar EntityNotFoundException quando não existe")
    void get_notFound() {
        Long id = 404L;
        when(sessionRepository.findById(id)).thenReturn(Optional.empty());

        var ex = assertThrows(EntityNotFoundException.class, () -> service.get(id));
        assertEquals("Session not found: " + id, ex.getMessage());

        verify(sessionRepository).findById(id);
        verifyNoMoreInteractions(sessionRepository);
        verifyNoInteractions(eventService, venueRepository);
    }

    @Test
    @DisplayName("listByEvent deve chamar findByEventId e mapear responses")
    void listByEvent_ok() {
        Long eventId = 1L;
        Venue v = venue(2L);
        Event e = event(eventId);

        when(sessionRepository.findByEventId(eventId)).thenReturn(List.of(
                session(10L, e, v),
                session(11L, e, v)
        ));

        List<SessionResponse> res = service.listByEvent(eventId);

        assertEquals(2, res.size());
        assertEquals(eventId, res.get(0).eventId());
        assertEquals(eventId, res.get(1).eventId());

        verify(sessionRepository).findByEventId(eventId);
        verifyNoMoreInteractions(sessionRepository);
        verifyNoInteractions(eventService, venueRepository);
    }

    @Test
    @DisplayName("update deve atualizar, salvar e retornar SessionResponse")
    void update_ok() {
        Long id = 10L;
        Long eventId = 1L;
        Long venueId = 2L;

        Event e = event(eventId);
        Venue v = venue(venueId);

        Session existing = session(id, event(99L), venue(98L));

        when(sessionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(eventService.findEntity(eventId)).thenReturn(e);
        when(venueRepository.findById(venueId)).thenReturn(Optional.of(v));
        when(sessionRepository.save(existing)).thenReturn(existing);

        SessionCreateRequest newReq = new SessionCreateRequest(
                eventId,
                venueId,
                LocalDateTime.of(2026, 3, 1, 20, 0),
                BigDecimal.valueOf(80)
        );

        SessionResponse res = service.update(id, newReq);

        assertEquals(id, res.id());
        assertEquals(eventId, res.eventId());
        assertEquals(venueId, res.venueId());
        assertEquals(LocalDateTime.of(2026, 3, 1, 20, 0), res.startsAt());
        assertEquals(BigDecimal.valueOf(80), res.price());

        assertEquals(eventId, existing.getEvent().getId());
        assertEquals(venueId, existing.getVenue().getId());
        assertEquals(LocalDateTime.of(2026, 3, 1, 20, 0), existing.getStartsAt());
        assertEquals(BigDecimal.valueOf(80), existing.getPrice());

        verify(sessionRepository).findById(id);
        verify(eventService).findEntity(eventId);
        verify(venueRepository).findById(venueId);
        verify(sessionRepository).save(existing);
        verifyNoMoreInteractions(sessionRepository, eventService, venueRepository);
    }

    @Test
    @DisplayName("update deve lançar EntityNotFoundException quando session não existe")
    void update_sessionNotFound() {
        Long id = 404L;
        when(sessionRepository.findById(id)).thenReturn(Optional.empty());

        var ex = assertThrows(EntityNotFoundException.class, () -> service.update(id, req(1L, 2L)));
        assertEquals("Session not found: " + id, ex.getMessage());

        verify(sessionRepository).findById(id);
        verifyNoMoreInteractions(sessionRepository);
        verifyNoInteractions(eventService, venueRepository);
    }

    @Test
    @DisplayName("update deve lançar EntityNotFoundException quando venue não existe")
    void update_venueNotFound() {
        Long id = 10L;
        Long eventId = 1L;
        Long venueId = 999L;

        Session existing = session(id, event(2L), venue(3L));

        when(sessionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(eventService.findEntity(eventId)).thenReturn(event(eventId));
        when(venueRepository.findById(venueId)).thenReturn(Optional.empty());

        var ex = assertThrows(EntityNotFoundException.class, () -> service.update(id, req(eventId, venueId)));
        assertEquals("Venue not found: " + venueId, ex.getMessage());

        verify(sessionRepository).findById(id);
        verify(eventService).findEntity(eventId);
        verify(venueRepository).findById(venueId);
        verify(sessionRepository, never()).save(any());
        verifyNoMoreInteractions(sessionRepository, eventService, venueRepository);
    }

    @Test
    @DisplayName("delete deve deletar quando existe")
    void delete_ok() {
        Long id = 7L;
        when(sessionRepository.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(sessionRepository).existsById(id);
        verify(sessionRepository).deleteById(id);
        verifyNoMoreInteractions(sessionRepository);
        verifyNoInteractions(eventService, venueRepository);
    }

    @Test
    @DisplayName("delete deve lançar EntityNotFoundException quando não existe")
    void delete_notFound() {
        Long id = 888L;
        when(sessionRepository.existsById(id)).thenReturn(false);

        var ex = assertThrows(EntityNotFoundException.class, () -> service.delete(id));
        assertEquals("Session not found: " + id, ex.getMessage());

        verify(sessionRepository).existsById(id);
        verify(sessionRepository, never()).deleteById(anyLong());
        verifyNoMoreInteractions(sessionRepository);
        verifyNoInteractions(eventService, venueRepository);
    }

    @Test
    @DisplayName("findEntity deve retornar Session quando existe")
    void findEntity_ok() {
        Long id = 1L;
        Session s = session(id, event(1L), venue(2L));

        when(sessionRepository.findById(id)).thenReturn(Optional.of(s));

        Session found = service.findEntity(id);

        assertSame(s, found);

        verify(sessionRepository).findById(id);
        verifyNoMoreInteractions(sessionRepository);
        verifyNoInteractions(eventService, venueRepository);
    }

    @Test
    @DisplayName("findEntity deve lançar EntityNotFoundException quando não existe")
    void findEntity_notFound() {
        Long id = 123L;
        when(sessionRepository.findById(id)).thenReturn(Optional.empty());

        var ex = assertThrows(EntityNotFoundException.class, () -> service.findEntity(id));
        assertEquals("Session not found: " + id, ex.getMessage());

        verify(sessionRepository).findById(id);
        verifyNoMoreInteractions(sessionRepository);
        verifyNoInteractions(eventService, venueRepository);
    }
}
