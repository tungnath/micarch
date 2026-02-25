package com.micarch.eventsourcing.events;

import java.math.BigDecimal;

// Domain events — stored in Axon Server event store; state is rebuilt by replaying these
public class OrderEvents {

    public record OrderPlacedEvent(
            String orderId,
            String customerId,
            String productId,
            int quantity,
            BigDecimal unitPrice
            ) {

    }

    public record OrderConfirmedEvent(String orderId) {

    }

    public record OrderCancelledEvent(String orderId, String reason) {

    }
}
