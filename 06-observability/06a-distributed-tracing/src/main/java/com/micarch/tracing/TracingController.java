package com.micarch.tracing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/tracing")
public class TracingController {
    private static final Logger log = LoggerFactory.getLogger(TracingController.class);

    @GetMapping("/checkout")
    public Map<String, String> checkout() {
        log.info("checkout flow started");
        return Map.of("operation", "checkout", "propagation", "W3C/Brave", "exporter", "Zipkin");
    }
}
