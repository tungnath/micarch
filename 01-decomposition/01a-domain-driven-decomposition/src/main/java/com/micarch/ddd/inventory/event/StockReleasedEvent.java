package com.micarch.ddd.inventory.event;

import com.micarch.ddd.shared.event.DomainEvent;

/**
 * Published when reserved stock is released (e.g., order cancelled).
 */
public record StockReleasedEvent(
        String orderId,
        String sku,
        int quantity
) implements DomainEvent {
}
