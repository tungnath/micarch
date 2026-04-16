package com.micarch.ddd.order.event;

import com.micarch.ddd.shared.event.DomainEvent;
import java.util.List;

/**
 * Published when an order is cancelled. Consumed by Inventory context
 * to release reserved stock.
 */
public record OrderCancelledEvent(
        String orderId,
        List<LineItemDetail> lineItems
) implements DomainEvent {

    public record LineItemDetail(
            String productId,
            int quantity
    ) {
    }
}
