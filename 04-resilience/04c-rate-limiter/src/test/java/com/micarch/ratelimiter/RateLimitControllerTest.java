package com.micarch.ratelimiter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitControllerTest {
    @Test
    void endpointReturnsAcceptedForAnAllowedCall() {
        assertThat(new RateLimitController().call().getBody()).isEqualTo("accepted");
    }
}
