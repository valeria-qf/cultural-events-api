package br.edu.ifrn.eventsapi.cultural_events_api.repository;

import br.edu.ifrn.eventsapi.cultural_events_api.model.Reservation;
import br.edu.ifrn.eventsapi.cultural_events_api.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByCustomerEmail(String customerEmail);

    Optional<Reservation> findByCode(UUID code);

    @Query("""
        select coalesce(sum(r.quantity), 0)
        from Reservation r
        where r.session.id = :sessionId and r.status = :status
    """)
    long sumQuantityBySessionAndStatus(@Param("sessionId") Long sessionId,
                                       @Param("status") ReservationStatus status);
}
