package br.edu.ifrn.eventsapi.cultural_events_api.service;

import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.ReservationCreateRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.dto.response.AvailabilityResponse;
import br.edu.ifrn.eventsapi.cultural_events_api.dto.response.ReservationResponse;
import br.edu.ifrn.eventsapi.cultural_events_api.model.*;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SessionService sessionService;

    @Transactional
    public ReservationResponse create(ReservationCreateRequest req) {
        Session session = sessionService.findEntity(req.sessionId());

        int capacity = session.getVenue().getCapacity();
        long reserved = reservationRepository.sumQuantityBySessionAndStatus(session.getId(), ReservationStatus.ACTIVE);
        long available = capacity - reserved;

        if (req.quantity() > available) {
            throw new IllegalArgumentException("Not enough seats. Available: " + available);
        }

        Reservation r = new Reservation();
        r.setSession(session);
        r.setCustomerName(req.customerName());
        r.setCustomerEmail(req.customerEmail());
        r.setQuantity(req.quantity());
        r.setStatus(ReservationStatus.ACTIVE);

        r = reservationRepository.save(r);
        return toResponse(r);
    }

    public List<ReservationResponse> list(String email) {
        if (email != null && !email.isBlank()) {
            return reservationRepository.findByCustomerEmail(email).stream().map(this::toResponse).toList();
        }
        return reservationRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ReservationResponse get(Long id) {
        Reservation r = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found: " + id));
        return toResponse(r);
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse availability(Long sessionId) {
        Session session = sessionService.findEntity(sessionId);
        int capacity = session.getVenue().getCapacity();
        long reserved = reservationRepository.sumQuantityBySessionAndStatus(sessionId, ReservationStatus.ACTIVE);
        long available = capacity - reserved;
        return new AvailabilityResponse(sessionId, capacity, reserved, Math.max(available, 0));
    }

    @Transactional
    public ReservationResponse cancel(Long id) {
        Reservation r = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found: " + id));
        r.setStatus(ReservationStatus.CANCELED);
        r = reservationRepository.save(r);
        return toResponse(r);
    }

    public ReservationResponse ticket(UUID code) {
        Reservation r = reservationRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found: " + code));
        return toResponse(r);
    }

    private ReservationResponse toResponse(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getSession().getId(),
                r.getCustomerName(),
                r.getCustomerEmail(),
                r.getQuantity(),
                r.getStatus(),
                r.getCode(),
                r.getCreatedAt()
        );
    }
}
