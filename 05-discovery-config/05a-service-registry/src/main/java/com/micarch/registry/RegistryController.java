package com.micarch.registry;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/registry")
public class RegistryController {
    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of("registry", "eureka", "selfRegistration", false, "evictionIntervalMs", 5000);
    }
}
