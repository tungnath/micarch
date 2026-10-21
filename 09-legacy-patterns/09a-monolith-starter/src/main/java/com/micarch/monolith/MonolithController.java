package com.micarch.monolith;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/monolith")
public class MonolithController {
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    @PostMapping("/orders")
    public Order place(@RequestBody Order order) {
        orders.put(order.id(), order);
        return order;
    }

    @GetMapping("/orders/{id}")
    public Order find(@PathVariable("id") String id) {
        return orders.get(id);
    }

    public record Order(String id, String customerId, String productId, int quantity, String state) { }
}
