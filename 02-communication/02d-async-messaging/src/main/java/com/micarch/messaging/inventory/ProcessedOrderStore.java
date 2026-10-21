package com.micarch.messaging.inventory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Demonstration idempotency store. Production code would use a durable table with a
 * unique constraint on the consumer name and event/order id.
 */
@Component
public class ProcessedOrderStore {

    private final Set<String> processedOrderIds = ConcurrentHashMap.newKeySet();

    public boolean contains(String orderId) {
        return processedOrderIds.contains(orderId);
    }

    public void markProcessed(String orderId) {
        processedOrderIds.add(orderId);
    }
}
