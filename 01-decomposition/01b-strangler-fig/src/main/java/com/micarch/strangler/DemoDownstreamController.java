package com.micarch.strangler;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * In-process downstreams for the {@code demo} profile. In production the same
 * gateway routes point to independent services; these endpoints only make the
 * migration behavior executable from a single module.
 */
@Profile("demo")
@RestController
class DemoDownstreamController {

    @PostMapping("/__strangler/new/orders")
    ResponseEntity<Map<String, Object>> createMigratedOrder(@RequestBody(required = false) Map<String, Object> request) {
        return ResponseEntity.ok(response("NEW_SERVICE", "/api/orders", request));
    }

    @GetMapping("/__strangler/new/orders/{orderId}")
    ResponseEntity<Map<String, Object>> getMigratedOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(response("NEW_SERVICE", "/api/orders/" + orderId, Map.of("orderId", orderId)));
    }

    @RequestMapping({"/__strangler/legacy", "/__strangler/legacy/**"})
    ResponseEntity<Map<String, Object>> legacyApi() {
        return ResponseEntity.ok(response("LEGACY_MONOLITH", "legacy-api", Map.of()));
    }

    private Map<String, Object> response(String owner, String route, Map<String, Object> request) {
        var response = new LinkedHashMap<String, Object>();
        response.put("handledBy", owner);
        response.put("route", route);
        response.put("receivedAt", Instant.now().toString());
        response.put("payload", request == null ? Map.of() : request);
        return response;
    }
}
