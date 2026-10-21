package com.micarch.circuitbreaker.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Small, intentionally flaky downstream used by the Docker demonstration.
 * It is enabled only when the {@code payment-target} profile is active.
 */
@Profile("payment-target")
@RestController
@RequestMapping("/api/payment")
public class PaymentTargetController {

    private final AtomicInteger calls = new AtomicInteger();

    @GetMapping("/{orderId}")
    public ResponseEntity<String> pay(@PathVariable String orderId,
                                      @RequestParam double amount) {
        int attempt = calls.incrementAndGet();
        if (attempt % 2 == 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"orderId\":\"" + orderId + "\",\"status\":\"FAILED\"}");
        }
        return ResponseEntity.ok("{\"orderId\":\"" + orderId
                + "\",\"amount\":" + amount + ",\"status\":\"PAID\"}");
    }
}
