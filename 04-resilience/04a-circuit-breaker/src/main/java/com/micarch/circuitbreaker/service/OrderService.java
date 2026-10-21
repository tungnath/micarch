package com.micarch.circuitbreaker.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

/**
 * Demonstrates the Circuit Breaker pattern.
 *
 * <p>
 * The @CircuitBreaker annotation wraps the method call. Resilience4j:
 * <ol>
 * <li>CLOSED state: calls pass through normally and failures are counted.</li>
 * <li>When failure rate exceeds threshold: transitions to OPEN — fallback is
 * immediately returned.</li>
 * <li>After waitDurationInOpenState: transitions to HALF_OPEN — one probe call
 * is allowed.</li>
 * <li>Probe succeeds → CLOSED; probe fails → OPEN again.</li>
 * </ol>
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final RestTemplate restTemplate;

    @Value("${payment.service.url:http://localhost:8402}")
    private String paymentServiceUrl;

    public OrderService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Calls the downstream Payment Service. The Circuit Breaker named
     * "paymentService" (configured in application.yml) protects this call. If
     * it fails repeatedly, the CB opens and the fallback is invoked
     * immediately.
     */
    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    public String processPayment(String orderId, double amount) {
        log.info("Calling payment service for order {}", orderId);
        // Flakey downstream — deliberately returns 500 every other call in the demo
        String response = restTemplate.getForObject(
                paymentServiceUrl + "/api/payment/{orderId}?amount={amount}",
                String.class, orderId, amount);
        log.info("Payment response: {}", response);
        return response;
    }

    /**
     * Fallback invoked when the Circuit Breaker is OPEN or the call fails.
     * Always accepts a Throwable as the last parameter.
     */
    public String paymentFallback(String orderId, double amount, Throwable ex) {
        log.warn("Circuit breaker OPEN for order {}. Returning fallback. Reason: {}",
                orderId, ex.getMessage());
        return String.format(
                "{\"orderId\":\"%s\", \"status\":\"PAYMENT_DEFERRED\", \"message\":\"Payment service unavailable. Will retry.\"}",
                orderId);
    }
}
