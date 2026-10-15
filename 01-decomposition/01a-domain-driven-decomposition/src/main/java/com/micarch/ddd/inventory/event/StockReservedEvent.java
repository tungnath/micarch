package com.micarch.ddd.inventory.event;

import com.micarch.ddd.shared.event.DomainEvent;

import java.math.BigDecimal;

/**
 * Published when stock is successfully reserved for an order.
 * Consumed by Payment context to initiate payment capture.
 */
public record StockReservedEvent(
        String orderId,
        BigDecimal totalAmount,
        String currency,
        String message
) implements DomainEvent {
}
