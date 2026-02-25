package com.micarch.saga.choreography.events;

import java.math.BigDecimal;

/**
 * Integration event published by Order service when an order is created.
 * Triggers the inventory reservation step in the choreographed saga.
 */
public record OrderCreatedEvent(
        String sagaId,
        String orderId,
        String customerId,
        String productId,
        int quantity,
        BigDecimal totalAmount
        ) {

}
