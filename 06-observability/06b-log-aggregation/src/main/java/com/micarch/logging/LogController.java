package com.micarch.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/logs")
public class LogController {
    private static final Logger log = LoggerFactory.getLogger(LogController.class);

    @GetMapping("/sample")
    public Map<String, String> sample() {
        log.info("structured log sample emitted");
        return Map.of("format", "JSON", "shipper", "Logstash", "search", "Elasticsearch");
    }
}
