package com.micarch.messaging.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer: publishes an OrderCreatedEvent to the 'order-events' topic.
 * Demonstrates: - Idempotent producer (enable.idempotence=true) - Acknowledged
 * sends (acks=all) - Async send callback with success/failure logging
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Async Messaging", description = "Kafka producer — publishes events for async consumers")
public class OrderProducerController {

    private static final Logger log = LoggerFactory.getLogger(OrderProducerController.class);
    private static final String TOPIC = "order-events";

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public record PlaceOrderRequest(String customerId, String productId, int quantity, BigDecimal unitPrice) {

    }

    @PostMapping
    @Operation(summary = "Place an order (async)",
            description = "Publishes OrderCreatedEvent to Kafka. Returns immediately — inventory and notifications are updated asynchronously.")
    public ResponseEntity<String> placeOrder(@RequestBody PlaceOrderRequest req) {
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId, req.customerId(), req.productId(), req.quantity(), req.unitPrice()
        );

        CompletableFuture<SendResult<String, OrderCreatedEvent>> future
                = kafkaTemplate.send(TOPIC, orderId, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("PRODUCER: Failed to publish order event for orderId={}: {}", orderId, ex.getMessage());
            } else {
                log.info("PRODUCER: Published OrderCreatedEvent orderId={} partition={} offset={}",
                        orderId,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });

        return ResponseEntity.accepted().body("Order accepted: " + orderId + " (processing asynchronously via Kafka)");
    }
}
