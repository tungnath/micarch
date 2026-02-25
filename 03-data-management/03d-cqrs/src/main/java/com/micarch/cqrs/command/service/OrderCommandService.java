package com.micarch.cqrs.command.service;

import com.micarch.cqrs.command.domain.OrderAggregate;
import com.micarch.cqrs.command.repository.OrderCommandRepository;
import com.micarch.cqrs.events.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * COMMAND side service: handles all write operations. Publishes OrderEvent to
 * Kafka after each state change. The QueryProjectionHandler on the read side
 * listens and updates MongoDB.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrderCommandService {

    private static final Logger log = LoggerFactory.getLogger(OrderCommandService.class);
    private static final String TOPIC = "order-events";

    private final OrderCommandRepository repository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public OrderAggregate placeOrder(String customerId, String productId, int quantity, BigDecimal unitPrice) {
        OrderAggregate order = OrderAggregate.create(customerId, productId, quantity, unitPrice);
        order = repository.save(order);
        log.info("COMMAND: Order placed id={}", order.getId());
        kafkaTemplate.send(TOPIC, order.getId().toString(),
                new OrderEvent("ORDER_PLACED", order.getId(), order.getCustomerId(),
                        order.getProductId(), order.getQuantity(), order.getTotalPrice(), order.getState().name()));
        return order;
    }

    public OrderAggregate confirmOrder(Long orderId) {
        OrderAggregate order = repository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        order.confirm();
        repository.save(order);
        log.info("COMMAND: Order confirmed id={}", orderId);
        kafkaTemplate.send(TOPIC, orderId.toString(),
                new OrderEvent("ORDER_CONFIRMED", order.getId(), order.getCustomerId(),
                        order.getProductId(), order.getQuantity(), order.getTotalPrice(), order.getState().name()));
        return order;
    }

    public OrderAggregate cancelOrder(Long orderId) {
        OrderAggregate order = repository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        order.cancel();
        repository.save(order);
        log.info("COMMAND: Order cancelled id={}", orderId);
        kafkaTemplate.send(TOPIC, orderId.toString(),
                new OrderEvent("ORDER_CANCELLED", order.getId(), order.getCustomerId(),
                        order.getProductId(), order.getQuantity(), order.getTotalPrice(), order.getState().name()));
        return order;
    }
}
