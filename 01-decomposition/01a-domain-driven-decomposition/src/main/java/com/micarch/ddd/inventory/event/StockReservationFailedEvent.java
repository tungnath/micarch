package com.micarch.ddd.inventory.event;

import com.micarch.ddd.shared.event.DomainEvent;

/**
 * Published when stock reservation fails (insufficient stock).
 * Consumed by Order context to auto-cancel the order.
 */
public record StockReservationFailedEvent(
        String orderId,
        String reason
) implements DomainEvent {
}
