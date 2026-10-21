package com.micarch.ratelimiter;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/limited")
public class RateLimitController {
    @GetMapping
    @RateLimiter(name = "publicApi")
    public ResponseEntity<String> call() {
        return ResponseEntity.ok("accepted");
    }
}
