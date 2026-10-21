package com.micarch.bff.service;

import java.math.BigDecimal;
import java.util.List;

public interface BffDataGateway {

    UserData user(String userId);

    List<OrderData> recentOrders(String userId);

    List<InventoryData> lowStockItems();

    List<PaymentData> pendingPayments(String userId);

    record UserData(String userId, String displayName, String email) {
    }

    record OrderData(String orderId, String status, BigDecimal totalAmount, String currency) {
    }

    record InventoryData(String sku, String productName, int availableStock) {
    }

    record PaymentData(String paymentId, BigDecimal amount, String currency, String status) {
    }
}
