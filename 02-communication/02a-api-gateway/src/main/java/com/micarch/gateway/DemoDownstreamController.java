package com.micarch.gateway;

import java.time.Instant;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** In-process downstreams used only by the demo profile. */
@Profile("demo")
@RestController
public class DemoDownstreamController {

    @PostMapping({"/__gateway-demo/orders", "/__gateway-demo/orders/**"})
    public Map<String, Object> createOrder(@RequestBody(required = false) Map<String, Object> request) {
        return response("ORDER_SERVICE", "/api/orders", request);
    }

    @GetMapping({"/__gateway-demo/orders", "/__gateway-demo/orders/{orderId}"})
    public Map<String, Object> getOrder(@PathVariable(name = "orderId", required = false) String orderId) {
        return response("ORDER_SERVICE", "/api/orders/" + (orderId == null ? "all" : orderId), Map.of());
    }

    @GetMapping({"/__gateway-demo/products", "/__gateway-demo/products/{productId}"})
    public Map<String, Object> getProduct(@PathVariable(name = "productId", required = false) String productId) {
        return response("PRODUCT_SERVICE", "/api/products/" + (productId == null ? "all" : productId), Map.of());
    }

    @GetMapping({"/__gateway-demo/users", "/__gateway-demo/users/{userId}"})
    public Map<String, Object> getUser(@PathVariable(name = "userId", required = false) String userId) {
        return response("USER_SERVICE", "/api/users/" + (userId == null ? "all" : userId), Map.of());
    }

    private Map<String, Object> response(String service, String route, Object payload) {
        return Map.of(
                "handledBy", service,
                "route", route,
                "receivedAt", Instant.now().toString(),
                "payload", payload == null ? Map.of() : payload);
    }
}
