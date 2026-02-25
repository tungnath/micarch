package com.micarch.cqrs.events;

import java.math.BigDecimal;

/**
 * Domain event published from the command side to Kafka. The query side listens
 * to this to update the read model (MongoDB projection).
 */
public record OrderEvent(
        String eventType, // ORDER_PLACED, ORDER_CONFIRMED, ORDER_CANCELLED
        Long orderId,
        String customerId,
        String productId,
        Integer quantity,
        BigDecimal totalPrice,
        String state
        ) {

}
