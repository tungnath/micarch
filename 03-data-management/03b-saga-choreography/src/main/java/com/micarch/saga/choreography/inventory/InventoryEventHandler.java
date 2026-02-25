package com.micarch.saga.choreography.inventory;

import com.micarch.saga.choreography.events.OrderCreatedEvent;
import com.micarch.saga.choreography.events.StockReservationFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Inventory Service saga participant (choreography).
 *
 * <p>
 * Listens for {@link OrderCreatedEvent} from the Order service. If stock is
 * available, reserves it and publishes {@code StockReservedEvent} to continue
 * the saga. If not, publishes {@link StockReservationFailedEvent} to trigger
 * the compensating saga.
 */
@Component
public class InventoryEventHandler {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventHandler.class);
    private static final String STOCK_RESERVED_TOPIC = "stock-events";
    private static final String STOCK_FAILED_TOPIC = "stock-failed-events";

    // Simulated in-memory stock store
    private final ConcurrentHashMap<String, Integer> stockMap = new ConcurrentHashMap<>();

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryEventHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        // Pre-load some stock
        stockMap.put("PROD-001", 100);
        stockMap.put("PROD-002", 5);
    }

    @KafkaListener(topics = "order-events", groupId = "inventory-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Inventory received OrderCreated saga event: {}", event.sagaId());

        int available = stockMap.getOrDefault(event.productId(), 0);
        if (available >= event.quantity()) {
            // Reserve stock
            stockMap.put(event.productId(), available - event.quantity());
            log.info("Stock reserved for product {} — remaining: {}", event.productId(), available - event.quantity());
            kafkaTemplate.send(STOCK_RESERVED_TOPIC, event.orderId(),
                    new StockReservedEvent(event.sagaId(), event.orderId(), event.productId(), event.quantity()));
        } else {
            // Compensating event — saga rollback
            log.warn("Insufficient stock for product {}. Publishing compensation event.", event.productId());
            kafkaTemplate.send(STOCK_FAILED_TOPIC, event.orderId(),
                    new StockReservationFailedEvent(event.sagaId(), event.orderId(), "Insufficient stock"));
        }
    }

    // Local DTO for the success event
    public record StockReservedEvent(String sagaId, String orderId, String productId, int reservedQuantity) {

    }
}
