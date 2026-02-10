package br.edu.ifrn.eventsapi.cultural_events_api.service;

import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.ReservationCreateRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.dto.response.AvailabilityResponse;
import br.edu.ifrn.eventsapi.cultural_events_api.dto.response.ReservationResponse;
import br.edu.ifrn.eventsapi.cultural_events_api.model.*;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    SessionService sessionService;

    @InjectMocks
    ReservationService service;

    private ReservationCreateRequest req(Long sessionId, int qty) {
        return new ReservationCreateRequest(
                sessionId,
                "Cliente 1",
                "cliente@ifrn.edu.br",
                qty
        );
    }

    private Venue venue(int capacity) {
        return Venue.builder()
                .id(1L)
                .name("Auditório Central")
                .address("IFRN")
                .capacity(capacity)
                .build();
    }

    private Session session(Long id, int capacity) {
        return Session.builder()
                .id(id)
                .venue(venue(capacity))
                .startsAt(LocalDateTime.of(2026, 2, 10, 19, 0))
                .price(BigDecimal.valueOf(50))
                .build();
    }

    private Reservation reservation(Long id, Session session, ReservationStatus status) {
        return Reservation.builder()
                .id(id)
                .session(session)
                .customerName("Cliente 1")
                .customerEmail("cliente@ifrn.edu.br")
                .quantity(2)
                .status(status)
                .code(UUID.randomUUID())
                .createdAt(LocalDateTime.of(2026, 2, 10, 10, 0))
                .build();
    }

    @Test
    @DisplayName("create deve criar reserva quando há vagas")
    void create_ok() {
        Long sessionId = 10L;
        Session s = session(sessionId, 100);

        when(sessionService.findEntity(sessionId)).thenReturn(s);
        when(reservationRepository.sumQuantityBySessionAndStatus(sessionId, ReservationStatus.ACTIVE))
                .thenReturn(20L);
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(inv -> {
                    Reservation r = inv.getArgument(0);
                    r.setId(1L);
                    if (r.getCode() == null) r.setCode(UUID.randomUUID());
                    if (r.getCreatedAt() == null) r.setCreatedAt(LocalDateTime.now());
                    return r;
                });

        ReservationResponse res = service.create(req(sessionId, 2));

        assertEquals(1L, res.id());
        assertEquals(sessionId, res.sessionId());
        assertEquals(2, res.quantity());
        assertEquals(ReservationStatus.ACTIVE, res.status());

        verify(sessionService).findEntity(sessionId);
        verify(reservationRepository).sumQuantityBySessionAndStatus(sessionId, ReservationStatus.ACTIVE);
        verify(reservationRepository).save(any(Reservation.class));
        verifyNoMoreInteractions(sessionService, reservationRepository);
    }

    @Test
    @DisplayName("create deve lançar erro quando não há vagas suficientes")
    void create_notEnoughSeats() {
        Long sessionId = 10L;
        Session s = session(sessionId, 10);

        when(sessionService.findEntity(sessionId)).thenReturn(s);
        when(reservationRepository.sumQuantityBySessionAndStatus(sessionId, ReservationStatus.ACTIVE))
                .thenReturn(9L);

        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(req(sessionId, 2)));

        assertEquals("Not enough seats. Available: 1", ex.getMessage());

        verify(sessionService).findEntity(sessionId);
        verify(reservationRepository).sumQuantityBySessionAndStatus(sessionId, ReservationStatus.ACTIVE);
        verify(reservationRepository, never()).save(any());
        verifyNoMoreInteractions(sessionService, reservationRepository);
    }

    @Test
    @DisplayName("list deve retornar por email quando informado")
    void list_withEmail() {
        String email = "cliente@ifrn.edu.br";
        Session s = session(1L, 100);

        when(reservationRepository.findByCustomerEmail(email))
                .thenReturn(List.of(reservation(1L, s, ReservationStatus.ACTIVE)));

        List<ReservationResponse> res = service.list(email);

        assertEquals(1, res.size());
        assertEquals(email, res.get(0).customerEmail());

        verify(reservationRepository).findByCustomerEmail(email);
        verifyNoMoreInteractions(reservationRepository);
        verifyNoInteractions(sessionService);
    }

    @Test
    @DisplayName("list deve retornar todos quando email é null")
    void list_withoutEmail() {
        Session s = session(1L, 100);

        when(reservationRepository.findAll())
                .thenReturn(List.of(
                        reservation(1L, s, ReservationStatus.ACTIVE),
                        reservation(2L, s, ReservationStatus.CANCELED)
                ));

        List<ReservationResponse> res = service.list(null);

        assertEquals(2, res.size());

        verify(reservationRepository).findAll();
        verifyNoMoreInteractions(reservationRepository);
        verifyNoInteractions(sessionService);
    }

    @Test
    @DisplayName("get deve retornar reserva quando existe")
    void get_ok() {
        Long id = 5L;
        Session s = session(10L, 50);
        Reservation r = reservation(id, s, ReservationStatus.ACTIVE);

        when(reservationRepository.findById(id)).thenReturn(Optional.of(r));

        ReservationResponse res = service.get(id);

        assertEquals(id, res.id());
        assertEquals(10L, res.sessionId());

        verify(reservationRepository).findById(id);
        verifyNoMoreInteractions(reservationRepository);
        verifyNoInteractions(sessionService);
    }

    @Test
    @DisplayName("get deve lançar erro quando não existe")
    void get_notFound() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        var ex = assertThrows(EntityNotFoundException.class, () -> service.get(999L));
        assertEquals("Reservation not found: 999", ex.getMessage());

        verify(reservationRepository).findById(999L);
        verifyNoMoreInteractions(reservationRepository);
        verifyNoInteractions(sessionService);
    }

    @Test
    @DisplayName("availability deve calcular corretamente")
    void availability_ok() {
        Long sessionId = 20L;

        when(sessionService.findEntity(sessionId)).thenReturn(session(sessionId, 100));
        when(reservationRepository.sumQuantityBySessionAndStatus(sessionId, ReservationStatus.ACTIVE))
                .thenReturn(30L);

        AvailabilityResponse res = service.availability(sessionId);

        assertEquals(100, res.capacity());
        assertEquals(30L, res.reservedActive());
        assertEquals(70L, res.available());

        verify(sessionService).findEntity(sessionId);
        verify(reservationRepository).sumQuantityBySessionAndStatus(sessionId, ReservationStatus.ACTIVE);
        verifyNoMoreInteractions(sessionService, reservationRepository);
    }

    @Test
    @DisplayName("cancel deve atualizar status para CANCELED")
    void cancel_ok() {
        Long id = 8L;
        Session s = session(1L, 10);
        Reservation r = reservation(id, s, ReservationStatus.ACTIVE);

        when(reservationRepository.findById(id)).thenReturn(Optional.of(r));
        when(reservationRepository.save(r)).thenReturn(r);

        ReservationResponse res = service.cancel(id);

        assertEquals(ReservationStatus.CANCELED, res.status());
        assertEquals(ReservationStatus.CANCELED, r.getStatus());

        verify(reservationRepository).findById(id);
        verify(reservationRepository).save(r);
        verifyNoMoreInteractions(reservationRepository);
        verifyNoInteractions(sessionService);
    }

    @Test
    @DisplayName("ticket deve retornar reserva pelo código")
    void ticket_ok() {
        UUID code = UUID.randomUUID();
        Session s = session(3L, 20);
        Reservation r = reservation(1L, s, ReservationStatus.ACTIVE);
        r.setCode(code);

        when(reservationRepository.findByCode(code)).thenReturn(Optional.of(r));

        ReservationResponse res = service.ticket(code);

        assertEquals(code, res.code());
        assertEquals(3L, res.sessionId());

        verify(reservationRepository).findByCode(code);
        verifyNoMoreInteractions(reservationRepository);
        verifyNoInteractions(sessionService);
    }
}
