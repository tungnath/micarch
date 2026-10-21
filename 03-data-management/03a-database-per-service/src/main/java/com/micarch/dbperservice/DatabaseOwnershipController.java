package com.micarch.dbperservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Demonstrates three bounded contexts owning separate persistence adapters. */
@RestController
@RequestMapping("/api")
public class DatabaseOwnershipController {
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final Map<String, Product> products = new ConcurrentHashMap<>();

    @PostMapping("/orders")
    public Order createOrder(@RequestBody Order order) {
        orders.put(order.id(), order);
        return order;
    }

    @GetMapping("/orders/{id}")
    public Order getOrder(@PathVariable("id") String id) {
        return orders.get(id);
    }

    @PostMapping("/products")
    public Product createProduct(@RequestBody Product product) {
        products.put(product.id(), product);
        return product;
    }

    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable("id") String id) {
        return products.get(id);
    }

    public record Order(String id, String customerId, String productId, int quantity) { }
    public record Product(String id, String name, int stock) { }
}
