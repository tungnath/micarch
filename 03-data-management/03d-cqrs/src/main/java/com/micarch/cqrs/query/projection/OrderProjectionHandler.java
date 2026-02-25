package com.micarch.cqrs.query.projection;

import com.micarch.cqrs.events.OrderEvent;
import com.micarch.cqrs.query.model.OrderView;
import com.micarch.cqrs.query.repository.OrderViewRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * QUERY SIDE: Projection handler. Listens to Kafka order-events and keeps the
 * MongoDB read model updated. This is the heart of CQRS — the read model is
 * eventually consistent with the write model.
 */
@Component
@RequiredArgsConstructor
public class OrderProjectionHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderProjectionHandler.class);
    private final OrderViewRepository viewRepository;

    @KafkaListener(topics = "order-events", groupId = "cqrs-projection-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void handle(OrderEvent event) {
        log.info("PROJECTION: Received {} for orderId={}", event.eventType(), event.orderId());

        switch (event.eventType()) {
            case "ORDER_PLACED" -> {
                OrderView view = new OrderView(
                        event.orderId(), event.customerId(), event.productId(),
                        event.quantity(), event.totalPrice(), event.state()
                );
                viewRepository.save(view);
                log.info("PROJECTION: Created OrderView for orderId={}", event.orderId());
            }

            case "ORDER_CONFIRMED", "ORDER_CANCELLED" -> {
                viewRepository.findByOrderId(event.orderId()).ifPresent(view -> {
                    view.updateState(event.state());
                    viewRepository.save(view);
                    log.info("PROJECTION: Updated OrderView state={} for orderId={}", event.state(), event.orderId());
                });
            }

            default ->
                log.warn("PROJECTION: Unknown event type: {}", event.eventType());
        }
    }
}
