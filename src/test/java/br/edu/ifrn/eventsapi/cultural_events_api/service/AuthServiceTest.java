package br.edu.ifrn.eventsapi.cultural_events_api.service;

import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.LoginRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.dto.request.RegisterRequest;
import br.edu.ifrn.eventsapi.cultural_events_api.dto.response.AuthResponse;
import br.edu.ifrn.eventsapi.cultural_events_api.model.Role;
import br.edu.ifrn.eventsapi.cultural_events_api.model.User;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldCreateUserAndReturnToken() {

        RegisterRequest req = new RegisterRequest("Valéria", "valeria@email.com", "123456");

        when(userRepository.existsByEmail(req.email())).thenReturn(false);
        when(encoder.encode(req.password())).thenReturn("HASHED");
        when(jwtService.generateToken(eq(req.email()), anyMap())).thenReturn("TOKEN");

        // simula o save devolvendo usuário com id
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            u.setId(1L);
            return u;
        });

        AuthResponse res = authService.register(req);

        assertEquals("TOKEN", res.token());
        assertEquals("Bearer", res.tokenType());
        assertEquals(1L, res.userId());
        assertEquals(req.email(), res.email());
        assertEquals(Role.USER.name(), res.role());


        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertEquals("Valéria", saved.getName());
        assertEquals("valeria@email.com", saved.getEmail());
        assertEquals("HASHED", saved.getPasswordHash());
        assertEquals(Role.USER, saved.getRole());

        verify(jwtService).generateToken(eq(req.email()), eq(Map.of("role", Role.USER.name())));
    }

    @Test
    void register_shouldThrowIfEmailAlreadyRegistered() {
        RegisterRequest req = new RegisterRequest("Valéria", "valeria@email.com", "123456");
        when(userRepository.existsByEmail(req.email())).thenReturn(true);

        assertThrows(EntityExistsException.class, () -> authService.register(req));

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(anyString(), anyMap());
        verify(encoder, never()).encode(anyString());
    }

    @Test
    void login_shouldReturnTokenWhenCredentialsOk() {
        LoginRequest req = new LoginRequest("valeria@email.com", "123456");

        User u = new User();
        u.setId(1L);
        u.setEmail(req.email());
        u.setPasswordHash("HASHED");
        u.setRole(Role.USER);

        when(userRepository.findByEmail(req.email())).thenReturn(Optional.of(u));
        when(encoder.matches(req.password(), u.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(eq(req.email()), anyMap())).thenReturn("TOKEN");

        AuthResponse res = authService.login(req);

        assertEquals("TOKEN", res.token());
        assertEquals("Bearer", res.tokenType());
        assertEquals(1L, res.userId());
        assertEquals(req.email(), res.email());
        assertEquals(Role.USER.name(), res.role());

        verify(jwtService).generateToken(eq(req.email()), eq(Map.of("role", Role.USER.name())));
    }

    @Test
    void login_shouldThrowWhenEmailNotFound() {
        LoginRequest req = new LoginRequest("nope@email.com", "123456");
        when(userRepository.findByEmail(req.email())).thenReturn(Optional.empty());

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> authService.login(req));

        assertEquals("Invalid credentials", ex.getMessage());
        verify(encoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString(), anyMap());
    }

    @Test
    void login_shouldThrowWhenPasswordInvalid() {
        LoginRequest req = new LoginRequest("valeria@email.com", "wrong");

        User u = new User();
        u.setEmail(req.email());
        u.setPasswordHash("HASHED");
        u.setRole(Role.USER);

        when(userRepository.findByEmail(req.email())).thenReturn(Optional.of(u));
        when(encoder.matches(req.password(), u.getPasswordHash())).thenReturn(false);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> authService.login(req));

        assertEquals("Invalid credentials", ex.getMessage());
        verify(jwtService, never()).generateToken(anyString(), anyMap());
    }
}
