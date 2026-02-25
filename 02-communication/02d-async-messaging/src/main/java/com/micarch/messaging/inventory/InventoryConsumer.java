package com.micarch.messaging.inventory;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.micarch.messaging.order.OrderCreatedEvent;

/**
 * Inventory consumer demonstrating: - @RetryableTopic: automatic retry with
 * backoff (non-blocking) - Dead Letter Topic (DLT): message moved to
 * 'order-events.DLT' after all retries fail - Manual acknowledgment: only ack
 * after successful processing - Idempotent processing: checks if reservation
 * already exists
 */
@Component
public class InventoryConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryConsumer.class);

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2.0, random = true),
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(topics = "order-events", groupId = "inventory-service-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, OrderCreatedEvent> record, Acknowledgment ack) {
        OrderCreatedEvent event = record.value();
        log.info("INVENTORY: Processing OrderCreatedEvent orderId={} productId={} qty={}",
                event.orderId(), event.productId(), event.quantity());

        try {
            // Simulate inventory reservation logic
            reserveStock(event.productId(), event.quantity());

            log.info("INVENTORY: Stock reserved for orderId={} productId={} qty={}",
                    event.orderId(), event.productId(), event.quantity());
            ack.acknowledge();

        } catch (InsufficientStockException e) {
            log.warn("INVENTORY: Insufficient stock for orderId={}: {}", event.orderId(), e.getMessage());
            // Don't ack — this message should NOT be retried (permanent failure)
            // In production, publish a StockReservationFailedEvent here
            ack.acknowledge(); // ack to avoid infinite loop; saga handles compensation
        }
    }

    // DLT handler: called when all retries are exhausted
    @KafkaListener(topics = "order-events.DLT", groupId = "inventory-dlt-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleDlt(ConsumerRecord<String, OrderCreatedEvent> record) {
        log.error("DLT: Message could not be processed after all retries. orderId={} key={}",
                record.value() != null ? record.value().orderId() : "unknown",
                record.key());
        // In production: alert, save to error store, or trigger manual review
    }

    private void reserveStock(String productId, int quantity) {
        // Simulate 20% failure rate to demonstrate retry behavior
        if (Math.random() < 0.2) {
            log.warn("INVENTORY: Simulated transient failure for productId={}", productId);
            throw new RuntimeException("Simulated transient DB error — will be retried");
        }
        log.info("INVENTORY: Reserved {} units of {}", quantity, productId);
    }

    static class InsufficientStockException extends RuntimeException {

        public InsufficientStockException(String message) {
            super(message);
        }
    }
}
