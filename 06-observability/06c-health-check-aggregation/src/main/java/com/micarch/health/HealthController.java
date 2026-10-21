package com.micarch.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    @GetMapping("/summary")
    public Map<String, Object> summary() {
        return Map.of("status", "UP", "services", Map.of("order-service", "UP", "payment-service", "UP"));
    }
}
