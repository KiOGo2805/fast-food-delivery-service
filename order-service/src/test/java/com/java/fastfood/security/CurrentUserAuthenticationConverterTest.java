package com.java.fastfood.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CurrentUserAuthenticationConverterTest {

    private final CurrentUserAuthenticationConverter converter =
            new CurrentUserAuthenticationConverter();

    @Test
    void convert_extractsPrincipalRolesAndAuthorities() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("preferred_username", "alice")
                .claim("realm_access", Map.of("roles", List.of("USER", "ADMIN")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        CurrentUserAuthenticationToken token =
                (CurrentUserAuthenticationToken) converter.convert(jwt);

        assertThat(token.getPrincipal()).isEqualTo(new CurrentUser("alice", List.of("USER", "ADMIN")));
        assertThat(token.getAuthorities())
                .containsExactlyInAnyOrder(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    void convert_withoutRealmRoles_usesEmptyRoleList() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("preferred_username", "alice")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        CurrentUserAuthenticationToken token =
                (CurrentUserAuthenticationToken) converter.convert(jwt);

        assertThat(token.getPrincipal()).isEqualTo(new CurrentUser("alice", List.of()));
        assertThat(token.getAuthorities()).isEmpty();
    }
}
