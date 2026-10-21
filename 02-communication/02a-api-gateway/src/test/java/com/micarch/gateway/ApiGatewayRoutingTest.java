package com.micarch.gateway;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.reactive.server.WebTestClient.bindToApplicationContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("demo")
class ApiGatewayRoutingTest {

    @Autowired
    private ApplicationContext applicationContext;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = bindToApplicationContext(applicationContext).configureClient().build();
    }

    @Test
    void protectedRouteRejectsMissingToken() {
        client.get()
                .uri("/api/products/P-1")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void authenticatedRouteReachesProductService() {
        client.get()
                .uri("/api/products/P-1")
                .header(AUTHORIZATION, "Bearer demo-token")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.handledBy").isEqualTo("PRODUCT_SERVICE");
    }

    @Test
    void publicUserRouteReachesUserService() {
        client.get()
                .uri("/api/users/U-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.handledBy").isEqualTo("USER_SERVICE");
    }
}
