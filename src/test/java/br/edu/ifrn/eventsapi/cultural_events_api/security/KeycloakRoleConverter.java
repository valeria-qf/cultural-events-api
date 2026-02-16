package br.edu.ifrn.eventsapi.cultural_events_api.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class KeycloakRoleConverterTest {

    private final KeycloakRoleConverter converter = new KeycloakRoleConverter();

    private Jwt jwtWithClaims(Map<String, Object> claims) {
        Map<String, Object> safeClaims = (claims == null || claims.isEmpty())
                ? Map.of("sub", "test-user")
                : claims;

        return new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                safeClaims
        );
    }

    private static Set<String> toAuthorityStrings(Collection<GrantedAuthority> authorities) {
        Set<String> out = new HashSet<>();
        for (GrantedAuthority a : authorities) out.add(a.getAuthority());
        return out;
    }

    @Test
    void should_convert_realm_access_roles() {
        Jwt jwt = jwtWithClaims(Map.of(
                "realm_access", Map.of("roles", List.of("USER", "ADMIN"))
        ));

        Set<String> auths = toAuthorityStrings(converter.convert(jwt));

        assertTrue(auths.contains("ROLE_USER"));
        assertTrue(auths.contains("ROLE_ADMIN"));
        assertEquals(2, auths.size());
    }

    @Test
    void should_convert_resource_access_client_roles() {
        Jwt jwt = jwtWithClaims(Map.of(
                "resource_access", Map.of(
                        "culturalevents-api", Map.of("roles", List.of("ORGANIZER"))
                )
        ));

        Set<String> auths = toAuthorityStrings(converter.convert(jwt));

        assertEquals(Set.of("ROLE_ORGANIZER"), auths);
    }

    @Test
    void should_merge_roles_from_realm_and_resource_access_without_duplicates() {
        Jwt jwt = jwtWithClaims(Map.of(
                "realm_access", Map.of("roles", List.of("USER", "ADMIN")),
                "resource_access", Map.of(
                        "culturalevents-api", Map.of("roles", List.of("ADMIN", "ORGANIZER"))
                )
        ));

        Set<String> auths = toAuthorityStrings(converter.convert(jwt));

        assertEquals(Set.of("ROLE_USER", "ROLE_ADMIN", "ROLE_ORGANIZER"), auths);
    }

    @Test
    void should_trim_and_ignore_blank_roles() {
        Jwt jwt = jwtWithClaims(Map.of(
                "realm_access", Map.of("roles", List.of("  USER  ", "", "   ")),
                "resource_access", Map.of(
                        "culturalevents-api", Map.of("roles", List.of("ADMIN", "   "))
                )
        ));

        Set<String> auths = toAuthorityStrings(converter.convert(jwt));

        assertEquals(Set.of("ROLE_USER", "ROLE_ADMIN"), auths);
    }

    @Test
    void should_keep_role_prefix_when_already_present() {
        Jwt jwt = jwtWithClaims(Map.of(
                "realm_access", Map.of("roles", List.of("ROLE_ADMIN", "USER"))
        ));

        Set<String> auths = toAuthorityStrings(converter.convert(jwt));

        assertEquals(Set.of("ROLE_ADMIN", "ROLE_USER"), auths);
    }

    @Test
    void should_return_empty_when_no_roles_claims_present() {
        Jwt jwt = jwtWithClaims(Map.of("sub", "test-user"));

        Set<String> auths = toAuthorityStrings(converter.convert(jwt));

        assertTrue(auths.isEmpty());
    }
}
