package com.micarch.bff.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("demo")
@Component
public class DemoBffDataGateway implements BffDataGateway {

    @Override
    public UserData user(String userId) {
        return new UserData(userId, "Demo Customer", userId.toLowerCase() + "@example.test");
    }

    @Override
    public List<OrderData> recentOrders(String userId) {
        return List.of(
                new OrderData("ORD-1001", "CONFIRMED", new BigDecimal("149.99"), "INR"),
                new OrderData("ORD-1002", "PENDING", new BigDecimal("49.99"), "INR"),
                new OrderData("ORD-1003", "SHIPPED", new BigDecimal("299.00"), "INR"));
    }

    @Override
    public List<InventoryData> lowStockItems() {
        return List.of(
                new InventoryData("SKU-1", "Laptop Stand", 3),
                new InventoryData("SKU-2", "USB-C Hub", 5));
    }

    @Override
    public List<PaymentData> pendingPayments(String userId) {
        return List.of(new PaymentData("PAY-1002", new BigDecimal("49.99"), "INR", "PENDING"));
    }
}
