package br.edu.ifrn.eventsapi.cultural_events_api.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET =
            "troque-isto-por-uma-string-bem-grande-aleatoria-1234567890";
    private static final long EXP_MINUTES = 2;

    @Test
    @DisplayName("generateToken + parseClaims deve retornar subject e claims esperados")
    void generateAndParse_shouldReturnExpectedClaims() {

        JwtService jwtService = new JwtService(SECRET, EXP_MINUTES);

        String email = "user@ifrn.edu.br";
        Map<String, Object> extraClaims = Map.of(
                "role", "ADMIN",
                "userId", 10L
        );

        Instant before = Instant.now();

        String token = jwtService.generateToken(email, extraClaims);
        Claims claims = jwtService.parseClaims(token);

        Instant after = Instant.now();

        assertNotNull(token);
        assertEquals(email, claims.getSubject());

        assertEquals("ADMIN", claims.get("role", String.class));

        Number userId = claims.get("userId", Number.class);
        assertNotNull(userId);
        assertEquals(10L, userId.longValue());

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());

        Instant iat = claims.getIssuedAt().toInstant();
        Instant exp = claims.getExpiration().toInstant();

        assertFalse(iat.isBefore(before.minusSeconds(2)));
        assertFalse(iat.isAfter(after.plusSeconds(2)));

        long diffSeconds = exp.getEpochSecond() - iat.getEpochSecond();
        assertTrue(diffSeconds >= (EXP_MINUTES * 60) - 2);
        assertTrue(diffSeconds <= (EXP_MINUTES * 60) + 2);
    }

    @Test
    @DisplayName("parseClaims deve falhar se o token for adulterado")
    void parseClaims_shouldFailWhenTokenIsTampered() {

        JwtService jwtService = new JwtService(SECRET, EXP_MINUTES);

        String token = jwtService.generateToken(
                "user@ifrn.edu.br",
                Map.of("role", "USER")
        );


        String tampered = token.substring(0, token.length() - 1) +
                (token.endsWith("a") ? "b" : "a");


        assertThrows(JwtException.class, () -> jwtService.parseClaims(tampered));
    }

    @Test
    @DisplayName("parseClaims deve falhar se a assinatura for de outra chave")
    void parseClaims_shouldFailWithDifferentSecret() {

        JwtService jwtService1 = new JwtService(SECRET, EXP_MINUTES);
        JwtService jwtService2 = new JwtService(SECRET + "_diferente", EXP_MINUTES);

        String token = jwtService1.generateToken(
                "user@ifrn.edu.br",
                Map.of("role", "USER")
        );

        assertThrows(JwtException.class, () -> jwtService2.parseClaims(token));
    }

    @Test
    @DisplayName("construtor deve falhar se secret for curta demais (evita config insegura)")
    void constructor_shouldFailWhenSecretIsTooShort() {

        String shortSecret = "muito-curta";

        assertThrows(RuntimeException.class, () -> new JwtService(shortSecret, EXP_MINUTES));
    }
}
