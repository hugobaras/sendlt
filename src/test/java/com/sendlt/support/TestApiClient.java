package com.sendlt.support;

import com.sendlt.api.dto.auth.AuthResponse;
import com.sendlt.api.dto.auth.LoginRequest;
import com.sendlt.api.dto.auth.RegisterRequest;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.http.MediaType;

public final class TestApiClient {

    private static final String DEFAULT_PASSWORD = "secret123";

    private TestApiClient() {}

    public static void register(RestTestClient client, String email, String displayName) {
        client.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegisterRequest(email, displayName, DEFAULT_PASSWORD))
                .exchange()
                .expectStatus()
                .isCreated();
    }

    public static String login(RestTestClient client, String email) {
        return client.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest(email, DEFAULT_PASSWORD))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody()
                .accessToken();
    }

    public static String registerAndLogin(RestTestClient client, String email, String displayName) {
        register(client, email, displayName);
        return login(client, email);
    }

    public static String bearer(String token) {
        return "Bearer " + token;
    }
}
