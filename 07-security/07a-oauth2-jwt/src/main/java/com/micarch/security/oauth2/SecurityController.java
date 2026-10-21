package com.micarch.security.oauth2;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SecurityController {
    @GetMapping("/profile")
    public Map<String, Object> profile(Authentication authentication) {
        String principal = authentication == null ? "anonymous" : authentication.getName();
        return Map.of("principal", principal, "authorization", "JWT scopes/roles");
    }
}
