package com.micarch.bff.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Production adapter seam; real services can expose compatible contracts. */
@Profile("!demo")
@Component
public class HttpBffDataGateway implements BffDataGateway {

    private final RestClient restClient;
    private final String orderServiceUrl;
    private final String inventoryServiceUrl;
    private final String paymentServiceUrl;

    public HttpBffDataGateway(
            RestClient.Builder builder,
            @Value("${services.order-service-url:http://localhost:8101}") String orderServiceUrl,
            @Value("${services.inventory-service-url:http://localhost:8301}") String inventoryServiceUrl,
            @Value("${services.payment-service-url:http://localhost:8401}") String paymentServiceUrl) {
        this.restClient = builder.build();
        this.orderServiceUrl = orderServiceUrl;
        this.inventoryServiceUrl = inventoryServiceUrl;
        this.paymentServiceUrl = paymentServiceUrl;
    }

    @Override
    public UserData user(String userId) {
        Map<?, ?> response = restClient.get().uri(orderServiceUrl + "/api/users/{userId}", userId)
                .retrieve().body(Map.class);
        return new UserData(userId, stringValue(response, "displayName", "Customer"),
                stringValue(response, "email", userId + "@example.test"));
    }

    @Override
    public List<OrderData> recentOrders(String userId) {
        Map<?, ?>[] response = restClient.get().uri(orderServiceUrl + "/api/orders?userId={userId}", userId)
                .retrieve().body(Map[].class);
        if (response == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(response)
                .map(order -> new OrderData(stringValue(order, "orderId", "unknown"),
                        stringValue(order, "status", "UNKNOWN"), decimalValue(order, "totalAmount"),
                        stringValue(order, "currency", "INR")))
                .toList();
    }

    @Override
    public List<InventoryData> lowStockItems() {
        Map<?, ?>[] response = restClient.get().uri(inventoryServiceUrl + "/api/inventory/low-stock")
                .retrieve().body(Map[].class);
        if (response == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(response)
                .map(item -> new InventoryData(stringValue(item, "sku", "unknown"),
                        stringValue(item, "productName", "unknown"), intValue(item, "availableStock")))
                .toList();
    }

    @Override
    public List<PaymentData> pendingPayments(String userId) {
        Map<?, ?>[] response = restClient.get()
                .uri(paymentServiceUrl + "/api/payments?userId={userId}&status=PENDING", userId)
                .retrieve().body(Map[].class);
        if (response == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(response)
                .map(payment -> new PaymentData(stringValue(payment, "paymentId", "unknown"),
                        decimalValue(payment, "amount"), stringValue(payment, "currency", "INR"),
                        stringValue(payment, "status", "UNKNOWN")))
                .toList();
    }

    private static String stringValue(Map<?, ?> values, String key, String fallback) {
        Object value = values == null ? null : values.get(key);
        return value == null ? fallback : value.toString();
    }

    private static BigDecimal decimalValue(Map<?, ?> values) {
        return decimalValue(values, "totalAmount");
    }

    private static BigDecimal decimalValue(Map<?, ?> values, String key) {
        Object value = values == null ? null : values.get(key);
        return value == null ? BigDecimal.ZERO : new BigDecimal(value.toString());
    }

    private static int intValue(Map<?, ?> values, String key) {
        Object value = values == null ? null : values.get(key);
        return value == null ? 0 : Integer.parseInt(value.toString());
    }
}
