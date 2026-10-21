package com.micarch.rest.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Profile("demo")
@RestController
public class DemoDownstreamController {

    @GetMapping("/api/inventory/{productId}")
    public Map<String, Object> inventory(@PathVariable("productId") String productId) {
        return Map.of("productId", productId, "availableStock", productId.equals("OUT-OF-STOCK") ? 0 : 20);
    }

    @PutMapping("/api/inventory/{productId}/reserve")
    public void reserve(@PathVariable("productId") String productId, @RequestParam("quantity") int quantity) {
        // Demo endpoint: the synchronous call boundary is the behavior under test.
    }

    @PostMapping("/api/payments/capture")
    public Map<String, String> capture(@RequestBody Map<String, Object> request) {
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String status = amount.compareTo(new BigDecimal("500.00")) > 0 ? "DECLINED" : "CAPTURED";
        return Map.of("paymentId", "PAY-DEMO", "status", status);
    }
}
