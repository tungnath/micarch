package com.micarch.bluegreen;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/deployment")
public class DeploymentController {
    @GetMapping("/state")
    public Map<String, Object> state() {
        return Map.of("active", "green", "blueTrafficPercent", 0, "rollback", "switch default route");
    }
}
