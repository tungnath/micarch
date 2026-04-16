package com.micarch.ddd.order.event;

import com.micarch.ddd.shared.event.DomainEvent;

/**
 * Published when an order is confirmed (after successful payment).
 */
public record OrderConfirmedEvent(
        String orderId,
        String customerId
) implements DomainEvent {
}
