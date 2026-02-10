package br.edu.ifrn.eventsapi.cultural_events_api.controller;

import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.LoginRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.RegisterRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.dto.response.AuthResponse;
import br.edu.ifrn.eventsapi.cultural_events_api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }
}
