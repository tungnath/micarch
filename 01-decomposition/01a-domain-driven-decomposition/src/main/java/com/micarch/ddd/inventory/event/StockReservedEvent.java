package com.micarch.ddd.inventory.event;

import com.micarch.ddd.shared.event.DomainEvent;

/**
 * Published when stock is successfully reserved for an order.
 * Consumed by Payment context to initiate payment capture.
 */
public record StockReservedEvent(
        String orderId,
        String message
) implements DomainEvent {
}
