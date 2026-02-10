package br.edu.ifrn.eventsapi.cultural_events_api.service;

import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.LoginRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.RegisterRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.dto.response.AuthResponse;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Role;
import br.edu.ifrn.eventsapi.cultural_events_api.model.User;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new EntityExistsException("Email already registered");
        }

        User u = User.builder()
                .name(req.name())
                .email(req.email())
                .passwordHash(encoder.encode(req.password()))
                .role(Role.USER)
                .build();

        u = userRepository.save(u);

        String token = jwtService.generateToken(u.getEmail(), Map.of("role", u.getRole().name()));
        return new AuthResponse(token, "Bearer", u.getId(), u.getEmail(), u.getRole().name());
    }

    public AuthResponse login(LoginRequest req) {
        User u = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!encoder.matches(req.password(), u.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtService.generateToken(u.getEmail(), Map.of("role", u.getRole().name()));
        return new AuthResponse(token, "Bearer", u.getId(), u.getEmail(), u.getRole().name());
    }
}
