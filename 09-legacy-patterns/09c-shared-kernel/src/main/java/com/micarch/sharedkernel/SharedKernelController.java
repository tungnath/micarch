package com.micarch.sharedkernel;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/shared-kernel")
public class SharedKernelController {
    @GetMapping("/money")
    public Map<String, Object> money() {
        return Map.of("amount", new BigDecimal("10.00"), "currency", "USD", "invariant", "currency required");
    }
}
