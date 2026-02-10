package br.edu.ifrn.eventsapi.cultural_events_api.repository;

import br.edu.ifrn.eventsapi.cultural_events_api.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByEventId(Long eventId);
}
