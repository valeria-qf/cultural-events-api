package br.edu.ifrn.eventsapi.cultural_events_api.repository;

import br.edu.ifrn.eventsapi.cultural_events_api.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {}
