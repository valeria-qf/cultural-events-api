package br.edu.ifrn.eventsapi.cultural_events_api.service;

import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.EventCreateRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.dto.response.EventResponse;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Event;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock EventRepository eventRepository;
    @InjectMocks EventService service;

    private static EventCreateRequest req() {
        return new EventCreateRequest(
                "Show Cultural",
                "Um evento incrível",
                "Música",
                LocalDate.of(2026, 2, 10),
                LocalDate.of(2026, 2, 11)
        );
    }

    private static Event event(Long id) {
        var r = req();
        return Event.builder()
                .id(id)
                .title(r.title())
                .description(r.description())
                .category(r.category())
                .startDate(r.startDate())
                .endDate(r.endDate())
                .build();
    }

    @Test
    @DisplayName("create: deve salvar e retornar EventResponse")
    void create_ok() {
        var r = req();

        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> {
            Event e = inv.getArgument(0, Event.class);
            e.setId(1L);
            return e;
        });

        EventResponse res = service.create(r);

        assertEquals(1L, res.id());
        assertEquals(r.title(), res.title());
        assertEquals(r.description(), res.description());
        assertEquals(r.category(), res.category());
        assertEquals(r.startDate(), res.startDate());
        assertEquals(r.endDate(), res.endDate());

        verify(eventRepository).save(any(Event.class));
        verifyNoMoreInteractions(eventRepository);
    }

    @Test
    @DisplayName("list: deve retornar lista de responses")
    void list_ok() {
        when(eventRepository.findAll()).thenReturn(List.of(event(1L), event(2L)));

        List<EventResponse> res = service.list();

        assertEquals(2, res.size());
        assertEquals(1L, res.get(0).id());
        assertEquals(2L, res.get(1).id());

        verify(eventRepository).findAll();
        verifyNoMoreInteractions(eventRepository);
    }

    @Test
    @DisplayName("get: deve retornar response quando existe")
    void get_ok() {
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event(10L)));

        EventResponse res = service.get(10L);

        assertEquals(10L, res.id());
        verify(eventRepository).findById(10L);
        verifyNoMoreInteractions(eventRepository);
    }

    @Test
    @DisplayName("get: deve lançar EntityNotFoundException quando não existe")
    void get_notFound() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        var ex = assertThrows(EntityNotFoundException.class, () -> service.get(999L));
        assertEquals("Event not found: 999", ex.getMessage());

        verify(eventRepository).findById(999L);
        verifyNoMoreInteractions(eventRepository);
    }

    @Test
    @DisplayName("update: deve atualizar, salvar e retornar response")
    void update_ok() {
        Long id = 5L;

        Event existing = Event.builder()
                .id(id)
                .title("Antigo")
                .description("desc antiga")
                .category("cat antiga")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 1, 2))
                .build();

        EventCreateRequest newReq = new EventCreateRequest(
                "Novo",
                "Nova desc",
                "Teatro",
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 12)
        );

        when(eventRepository.findById(id)).thenReturn(Optional.of(existing));
        when(eventRepository.save(existing)).thenReturn(existing);

        EventResponse res = service.update(id, newReq);

        assertEquals(id, res.id());
        assertEquals("Novo", res.title());
        assertEquals("Nova desc", res.description());
        assertEquals("Teatro", res.category());
        assertEquals(LocalDate.of(2026, 4, 10), res.startDate());
        assertEquals(LocalDate.of(2026, 4, 12), res.endDate());

        verify(eventRepository).findById(id);
        verify(eventRepository).save(existing);
        verifyNoMoreInteractions(eventRepository);
    }

    @Test
    @DisplayName("update: deve lançar EntityNotFoundException quando não existe")
    void update_notFound() {
        when(eventRepository.findById(404L)).thenReturn(Optional.empty());

        var ex = assertThrows(EntityNotFoundException.class, () -> service.update(404L, req()));
        assertEquals("Event not found: 404", ex.getMessage());

        verify(eventRepository).findById(404L);
        verifyNoMoreInteractions(eventRepository);
    }

    @Test
    @DisplayName("delete: deve deletar quando existe")
    void delete_ok() {
        when(eventRepository.existsById(7L)).thenReturn(true);

        service.delete(7L);

        verify(eventRepository).existsById(7L);
        verify(eventRepository).deleteById(7L);
        verifyNoMoreInteractions(eventRepository);
    }

    @Test
    @DisplayName("delete: deve lançar EntityNotFoundException quando não existe")
    void delete_notFound() {
        when(eventRepository.existsById(888L)).thenReturn(false);

        var ex = assertThrows(EntityNotFoundException.class, () -> service.delete(888L));
        assertEquals("Event not found: 888", ex.getMessage());

        verify(eventRepository).existsById(888L);
        verify(eventRepository, never()).deleteById(anyLong());
        verifyNoMoreInteractions(eventRepository);
    }

    @Test
    @DisplayName("findEntity: deve retornar entidade quando existe")
    void findEntity_ok() {
        Event e = event(3L);
        when(eventRepository.findById(3L)).thenReturn(Optional.of(e));

        Event found = service.findEntity(3L);

        assertSame(e, found);
        verify(eventRepository).findById(3L);
        verifyNoMoreInteractions(eventRepository);
    }

    @Test
    @DisplayName("findEntity: deve lançar EntityNotFoundException quando não existe")
    void findEntity_notFound() {
        when(eventRepository.findById(123L)).thenReturn(Optional.empty());

        var ex = assertThrows(EntityNotFoundException.class, () -> service.findEntity(123L));
        assertEquals("Event not found: 123", ex.getMessage());

        verify(eventRepository).findById(123L);
        verifyNoMoreInteractions(eventRepository);
    }
}
