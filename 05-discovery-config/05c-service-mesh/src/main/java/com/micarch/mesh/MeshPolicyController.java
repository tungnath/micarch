package com.micarch.mesh;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/mesh")
public class MeshPolicyController {
    @GetMapping("/policy")
    public Map<String, Object> policy() {
        return Map.of("mtls", "STRICT", "retries", 3, "circuitBreaker", true, "owner", "sidecar");
    }
}
