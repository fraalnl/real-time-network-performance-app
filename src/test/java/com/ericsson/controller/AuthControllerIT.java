package com.ericsson.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/auth/login";
    }

    @Test
    void testLoginWithValidCredentials() {
        Map<String, String> request = Map.of(
                "username", "admin",
                "password", "admin123"
        );

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        Map<String, String> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).containsKeys("token", "role");
        assertThat(body.get("role")).isEqualTo("ROLE_ADMIN");
        assertThat(body.get("token")).isNotBlank();
    }

    @Test
    void testLoginWithInvalidPassword() {
        Map<String, String> request = Map.of(
                "username", "admin",
                "password", "wrongpassword"
        );

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getStatusCode().value()).isEqualTo(401);

        Map<String, String> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).containsKey("error");
        assertThat(body.get("error")).isEqualTo("Invalid credentials. Please try again.");
    }

    @Test
    void testLoginWithUnknownUser() {
        Map<String, String> request = Map.of(
                "username", "unknownuser",
                "password", "somepassword"
        );

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getStatusCode().value()).isEqualTo(401);

        Map<String, String> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).containsKey("error");
        assertThat(body.get("error")).isEqualTo("Invalid credentials. Please try again.");
    }
}
