package br.edu.ifrn.eventsapi.cultural_events_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class CulturalEventsApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CulturalEventsApiApplication.class, args);
	}
}
