package com.micarch.canary;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/deployment/canary")
public class CanaryController {
    private final AtomicInteger weight = new AtomicInteger(5);

    @GetMapping
    public Map<String, Object> status() {
        return Map.of("canaryWeightPercent", weight.get(), "stableWeightPercent", 100 - weight.get());
    }

    @PostMapping("/weight")
    public Map<String, Object> setWeight(@RequestParam("percent") int percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("percent must be between 0 and 100");
        }
        weight.set(percent);
        return status();
    }
}
