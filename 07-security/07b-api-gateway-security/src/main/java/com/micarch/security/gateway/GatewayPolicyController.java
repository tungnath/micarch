package com.micarch.security.gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/public")
public class GatewayPolicyController {
    @GetMapping("/security-policy")
    public Map<String, Object> policy() {
        return Map.of("publicPaths", new String[]{"/public/**", "/actuator/health"}, "protectedDefault", true);
    }
}
