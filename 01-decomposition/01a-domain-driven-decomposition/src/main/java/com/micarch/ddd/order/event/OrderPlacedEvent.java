package com.micarch.ddd.order.event;

import com.micarch.ddd.shared.event.DomainEvent;
import java.math.BigDecimal;
import java.util.List;

/**
 * Published when a new order is placed. This event crosses the bounded context
 * boundary — consumed by Inventory (to reserve stock) and potentially by
 * Notification services.
 */
public record OrderPlacedEvent(
        String orderId,
        String customerId,
        List<LineItemDetail> lineItems,
        BigDecimal totalAmount,
        String currency
) implements DomainEvent {

    public record LineItemDetail(
            String productId,
            String productName,
            int quantity
    ) {
    }
}
