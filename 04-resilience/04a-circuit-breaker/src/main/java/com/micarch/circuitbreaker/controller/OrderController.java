package com.micarch.circuitbreaker.controller;

import com.micarch.circuitbreaker.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * Controller exposing the Circuit Breaker demo endpoint.
 *
 * <p>
 * Try the following to see the CB state machine in action:
 * <ol>
 * <li>Start this service (port 8401) WITHOUT the payment service (port
 * 8402).</li>
 * <li>Call POST /api/orders/{orderId}/pay repeatedly.</li>
 * <li>After 5 failures (50% of sliding window), the CB opens — calls return
 * instantly with fallback.</li>
 * <li>After 10 seconds, CB moves to HALF_OPEN — one call is tried.</li>
 * <li>Start the payment service — the probe succeeds and CB closes again.</li>
 * </ol>
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Circuit Breaker Demo", description = "Demonstrates Resilience4j CircuitBreaker pattern")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/{orderId}/pay")
    @Operation(
            summary = "Process payment with Circuit Breaker",
            description = "Calls downstream payment service. CB opens after repeated failures.")
    public String processPayment(@PathVariable String orderId, @RequestParam double amount) {
        return orderService.processPayment(orderId, amount);
    }
}
