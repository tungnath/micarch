package com.micarch.messaging.order;

import java.math.BigDecimal;

/**
 * Domain event published to Kafka when a new order is placed. Consumers:
 * InventoryConsumer, NotificationConsumer.
 */
public record OrderCreatedEvent(
        String orderId,
        String customerId,
        String productId,
        int quantity,
        BigDecimal unitPrice
        ) {

}
