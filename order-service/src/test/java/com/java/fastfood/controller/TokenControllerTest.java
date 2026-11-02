package com.java.fastfood.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TokenControllerTest {

    @Test
    void token_returnsAccessTokenValue() {
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "access-token",
                Instant.now(),
                Instant.now().plusSeconds(60));
        ClientRegistration registration = ClientRegistration.withRegistrationId("keycloak")
                .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientId("client")
                .authorizationUri("https://example.test/authorize")
                .tokenUri("https://example.test/token")
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .build();
        OAuth2AuthorizedClient client = new OAuth2AuthorizedClient(
                registration, "alice", accessToken);

        Map<String, String> response = new TokenController().token(client);

        assertThat(response).containsEntry("access_token", "access-token");
    }
}
