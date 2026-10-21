package com.micarch.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {
    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of("backend", "git", "refreshEndpoint", "/actuator/refresh", "immutableArtifacts", true);
    }
}
