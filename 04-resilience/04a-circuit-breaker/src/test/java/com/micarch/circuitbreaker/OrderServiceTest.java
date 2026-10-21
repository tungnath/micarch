package com.micarch.circuitbreaker;

import com.micarch.circuitbreaker.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class OrderServiceTest {
    @Test
    void fallbackReturnsAnExplicitDeferredPaymentState() {
        var result = new OrderService(new RestTemplate()).paymentFallback("O-1", 10, new RuntimeException("down"));
        assertThat(result).contains("PAYMENT_DEFERRED").contains("O-1");
    }
}
