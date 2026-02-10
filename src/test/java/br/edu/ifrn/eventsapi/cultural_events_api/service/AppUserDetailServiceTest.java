package br.edu.ifrn.eventsapi.cultural_events_api.service;

import br.edu.ifrn.eventsapi.cultural_events_api.model.Role;
import br.edu.ifrn.eventsapi.cultural_events_api.model.User;
import br.edu.ifrn.eventsapi.cultural_events_api.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppUserDetailsService service;

    @Test
    @DisplayName("loadUserByUsername deve retornar UserDetails com ROLE_ + role quando usuário existe")
    void loadUserByUsername_whenUserExists_returnsUserDetailsWithRole() {
        // Arrange
        String email = "valeria@ifrn.edu.br";
        String passwordHash = "{bcrypt}$2a$10$abc123";
        Role role = Role.ADMIN;

        User user = User.builder()
                .id(1L)
                .name("Valéria")
                .email(email)
                .passwordHash(passwordHash)
                .role(role)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(user));

        UserDetails details = service.loadUserByUsername(email);

        assertNotNull(details);
        assertEquals(email, details.getUsername());
        assertEquals(passwordHash, details.getPassword());

        var authorities = details.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toSet());

        assertTrue(authorities.contains("ROLE_ADMIN"));
        assertEquals(1, authorities.size(), "Deveria ter apenas 1 authority");

        verify(userRepository, times(1)).findByEmail(email);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("loadUserByUsername deve lançar UsernameNotFoundException quando usuário não existe")
    void loadUserByUsername_whenUserNotFound_throwsException() {

        String email = "naoexiste@ifrn.edu.br";
        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.empty());

        var ex = assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername(email));

        assertEquals("User not found: " + email, ex.getMessage());

        verify(userRepository, times(1)).findByEmail(email);
        verifyNoMoreInteractions(userRepository);
    }
}
