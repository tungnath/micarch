package com.micarch.ambassador;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ambassador")
public class AmbassadorController {
    @GetMapping("/legacy-order")
    @Retry(name = "legacySystem")
    @CircuitBreaker(name = "legacySystem", fallbackMethod = "fallback")
    public Map<String, String> legacyOrder() {
        return Map.of("protocol", "REST-to-SOAP adapter", "status", "forwarded");
    }

    public Map<String, String> fallback(Throwable failure) {
        return Map.of("protocol", "REST-to-SOAP adapter", "status", "deferred");
    }
}
