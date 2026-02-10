package br.edu.ifrn.eventsapi.cultural_events_api.service;

import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.VenueCreateRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.dto.response.VenueResponse;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Venue;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.VenueRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock
    VenueRepository venueRepository;

    @InjectMocks
    VenueService service;

    private VenueCreateRequest req() {
        return new VenueCreateRequest("Auditório Central", "IFRN - Campus", 500);
    }

    private Venue venue(Long id) {
        return Venue.builder()
                .id(id)
                .name("Auditório Central")
                .address("IFRN - Campus")
                .capacity(500)
                .build();
    }

    @Test
    @DisplayName("create deve salvar e retornar VenueResponse")
    void create_ok() {
        when(venueRepository.save(any(Venue.class))).thenAnswer(inv -> {
            Venue v = inv.getArgument(0);
            v.setId(1L);
            return v;
        });

        VenueResponse res = service.create(req());

        assertEquals(1L, res.id());
        assertEquals("Auditório Central", res.name());
        assertEquals("IFRN - Campus", res.address());
        assertEquals(500, res.capacity());

        verify(venueRepository).save(any(Venue.class));
        verifyNoMoreInteractions(venueRepository);
    }

    @Test
    @DisplayName("list deve retornar lista de VenueResponse")
    void list_ok() {
        when(venueRepository.findAll()).thenReturn(List.of(venue(1L), venue(2L)));

        List<VenueResponse> res = service.list();

        assertEquals(2, res.size());
        assertEquals(1L, res.get(0).id());
        assertEquals(2L, res.get(1).id());

        verify(venueRepository).findAll();
        verifyNoMoreInteractions(venueRepository);
    }

    @Test
    @DisplayName("get deve retornar VenueResponse quando existe")
    void get_ok() {
        when(venueRepository.findById(10L)).thenReturn(Optional.of(venue(10L)));

        VenueResponse res = service.get(10L);

        assertEquals(10L, res.id());
        assertEquals("Auditório Central", res.name());

        verify(venueRepository).findById(10L);
        verifyNoMoreInteractions(venueRepository);
    }

    @Test
    @DisplayName("get deve lançar EntityNotFoundException quando não existe")
    void get_notFound() {
        when(venueRepository.findById(999L)).thenReturn(Optional.empty());

        var ex = assertThrows(EntityNotFoundException.class, () -> service.get(999L));
        assertEquals("Venue not found: 999", ex.getMessage());

        verify(venueRepository).findById(999L);
        verifyNoMoreInteractions(venueRepository);
    }

    @Test
    @DisplayName("update deve atualizar, salvar e retornar VenueResponse")
    void update_ok() {
        Long id = 5L;
        Venue existing = Venue.builder()
                .id(id)
                .name("Antigo")
                .address("End antigo")
                .capacity(100)
                .build();

        VenueCreateRequest newReq = new VenueCreateRequest("Novo", "End novo", 700);

        when(venueRepository.findById(id)).thenReturn(Optional.of(existing));
        when(venueRepository.save(existing)).thenReturn(existing);

        VenueResponse res = service.update(id, newReq);

        assertEquals(id, res.id());
        assertEquals("Novo", res.name());
        assertEquals("End novo", res.address());
        assertEquals(700, res.capacity());

        assertEquals("Novo", existing.getName());
        assertEquals("End novo", existing.getAddress());
        assertEquals(700, existing.getCapacity());

        verify(venueRepository).findById(id);
        verify(venueRepository).save(existing);
        verifyNoMoreInteractions(venueRepository);
    }

    @Test
    @DisplayName("update deve lançar EntityNotFoundException quando venue não existe")
    void update_notFound() {
        when(venueRepository.findById(404L)).thenReturn(Optional.empty());

        var ex = assertThrows(EntityNotFoundException.class, () -> service.update(404L, req()));
        assertEquals("Venue not found: 404", ex.getMessage());

        verify(venueRepository).findById(404L);
        verifyNoMoreInteractions(venueRepository);
    }

    @Test
    @DisplayName("delete deve deletar quando existe")
    void delete_ok() {
        when(venueRepository.existsById(7L)).thenReturn(true);

        service.delete(7L);

        verify(venueRepository).existsById(7L);
        verify(venueRepository).deleteById(7L);
        verifyNoMoreInteractions(venueRepository);
    }

    @Test
    @DisplayName("delete deve lançar EntityNotFoundException quando não existe")
    void delete_notFound() {
        when(venueRepository.existsById(888L)).thenReturn(false);

        var ex = assertThrows(EntityNotFoundException.class, () -> service.delete(888L));
        assertEquals("Venue not found: 888", ex.getMessage());

        verify(venueRepository).existsById(888L);
        verify(venueRepository, never()).deleteById(anyLong());
        verifyNoMoreInteractions(venueRepository);
    }
}
