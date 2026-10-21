package com.micarch.sidecar;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/sidecar")
public class SidecarController {
    @GetMapping("/policy")
    public Map<String, Object> policy() {
        return Map.of("proxy", "envoy", "mTLS", true, "retries", 2, "tracing", true);
    }
}
