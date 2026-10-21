package com.micarch.shareddb;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Intentionally exposes order and inventory data from one shared store. */
@RestController
@RequestMapping("/api/shared")
public class SharedOrderController {
    private final Map<String, SharedOrder> sharedTable = new ConcurrentHashMap<>();

    @PostMapping("/orders")
    public SharedOrder save(@RequestBody SharedOrder order) {
        sharedTable.put(order.id(), order);
        return order;
    }

    @GetMapping("/orders/{id}")
    public SharedOrder find(@PathVariable("id") String id) {
        return sharedTable.get(id);
    }

    public record SharedOrder(String id, String productId, int quantity, String state) { }
}
