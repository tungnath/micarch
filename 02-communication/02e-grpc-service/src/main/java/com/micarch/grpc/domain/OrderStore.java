package com.micarch.grpc.domain;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.micarch.grpc.api.OrderResponse;

@Component
public class OrderStore {

    private final Map<String, OrderResponse> orders = new ConcurrentHashMap<>();

    public void save(OrderResponse order) {
        orders.put(order.getOrderId(), order);
    }

    public OrderResponse find(String orderId) {
        return orders.get(orderId);
    }

    public List<OrderResponse> findByCustomer(String customerId) {
        return orders.values().stream()
                .filter(order -> customerId.isBlank() || order.getCustomerId().equals(customerId))
                .sorted(java.util.Comparator.comparing(OrderResponse::getOrderId))
                .toList();
    }
}
