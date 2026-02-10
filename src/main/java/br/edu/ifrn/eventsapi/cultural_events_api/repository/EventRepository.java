package br.edu.ifrn.eventsapi.cultural_events_api.repository;


import br.edu.ifrn.eventsapi.cultural_events_api.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {}
