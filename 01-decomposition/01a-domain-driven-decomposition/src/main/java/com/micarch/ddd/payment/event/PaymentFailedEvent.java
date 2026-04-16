package com.micarch.ddd.payment.event;

import com.micarch.ddd.shared.event.DomainEvent;

/**
 * Published when payment processing fails.
 * Consumed by Order context to auto-cancel the order.
 */
public record PaymentFailedEvent(
        String paymentId,
        String orderId,
        String reason
) implements DomainEvent {
}
