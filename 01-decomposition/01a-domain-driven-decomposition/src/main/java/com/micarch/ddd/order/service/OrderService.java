package com.micarch.ddd.order.service;

import com.micarch.ddd.order.domain.Order;
import com.micarch.ddd.order.domain.OrderPlacedEvent;
import com.micarch.ddd.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Application service for the Order bounded context. Orchestrates domain logic
 * and publishes domain events. No knowledge of Inventory or Payment internals.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final String ORDER_TOPIC = "order-events";

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public OrderService(OrderRepository orderRepository,
            KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public Order placeOrder(String customerId, String productId, int quantity, BigDecimal unitPrice) {
        var order = Order.create(customerId, productId, quantity, unitPrice);
        orderRepository.save(order);

        // Publish domain event — decoupled cross-context communication
        var event = new OrderPlacedEvent(
                order.getOrderId(), customerId, productId, quantity, order.getTotalAmount());
        kafkaTemplate.send(ORDER_TOPIC, order.getOrderId(), event);

        log.info("Order placed: {} for customer {}", order.getOrderId(), customerId);
        return order;
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Order findById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    @Transactional
    public Order confirmOrder(String orderId) {
        var order = findById(orderId);
        order.confirm();
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(String orderId) {
        var order = findById(orderId);
        order.cancel();
        return orderRepository.save(order);
    }
}
