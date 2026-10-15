package com.micarch.strangler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("demo")
class StranglerFigRoutingTest {

    @Autowired
    private WebTestClient client;

    @Test
    void migratedOrderRouteGoesToNewService() {
        var body = client.post()
                .uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("customerId", "CUST-1"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(body).containsEntry("handledBy", "NEW_SERVICE");
    }

    @Test
    void unmigratedRouteFallsBackToLegacyMonolith() {
        var body = client.get()
                .uri("/api/customers/CUST-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(body).containsEntry("handledBy", "LEGACY_MONOLITH");
    }
}
