package com.micarch.ddd.order.domain;

import java.math.BigDecimal;

/**
 * Domain event published when an order is placed. This is the ONLY way the
 * Order Bounded Context communicates with Inventory and Payment bounded
 * contexts.
 */
public record OrderPlacedEvent(
        String orderId,
        String customerId,
        String productId,
        int quantity,
        BigDecimal totalAmount
        ) {

}
