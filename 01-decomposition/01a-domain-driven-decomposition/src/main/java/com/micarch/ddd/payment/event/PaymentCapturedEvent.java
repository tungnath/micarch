package com.micarch.ddd.payment.event;

import com.micarch.ddd.shared.event.DomainEvent;
import java.math.BigDecimal;

/**
 * Published when payment is successfully captured.
 * Consumed by Order context to auto-confirm the order.
 */
public record PaymentCapturedEvent(
        String paymentId,
        String orderId,
        BigDecimal amount,
        String currency
) implements DomainEvent {
}
