package com.micarch.rest.domain;

import java.math.BigDecimal;
import java.util.UUID;

import com.micarch.rest.client.InventoryClient;
import com.micarch.rest.client.PaymentClient;
import org.springframework.stereotype.Service;

@Service
public class SyncOrderService {

    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;

    public SyncOrderService(InventoryClient inventoryClient, PaymentClient paymentClient) {
        this.inventoryClient = inventoryClient;
        this.paymentClient = paymentClient;
    }

    public OrderResult placeOrder(String productId, int quantity, BigDecimal unitPrice) {
        if (quantity <= 0 || unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Quantity must be positive and price must not be negative");
        }

        InventoryClient.StockResponse stock = inventoryClient.checkStock(productId);
        if (!stock.available(quantity)) {
            return OrderResult.rejected("INSUFFICIENT_STOCK", "Inventory has only " + stock.availableStock());
        }

        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        inventoryClient.reserveStock(productId, quantity);

        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));
        PaymentClient.PaymentResponse payment = paymentClient.capture(
                new PaymentClient.PaymentRequest(orderId, total, "INR"));
        if (!payment.captured()) {
            return OrderResult.rejected("PAYMENT_FAILED", "Payment status: " + payment.status());
        }
        return new OrderResult(orderId, "CONFIRMED", payment.paymentId(), total, null);
    }

    public record OrderResult(String orderId, String status, String paymentId,
                              BigDecimal totalAmount, String rejectionReason) {
        public static OrderResult rejected(String reason, String detail) {
            return new OrderResult(null, "REJECTED", null, BigDecimal.ZERO, reason + ": " + detail);
        }
    }
}
