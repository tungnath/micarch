package com.micarch.ddd.order.service;

import com.micarch.ddd.order.domain.*;
import com.micarch.ddd.order.event.OrderCancelledEvent;
import com.micarch.ddd.order.event.OrderConfirmedEvent;
import com.micarch.ddd.order.event.OrderPlacedEvent;
import com.micarch.ddd.order.repository.OrderRepository;
import com.micarch.ddd.inventory.event.StockReservationFailedEvent;
import com.micarch.ddd.payment.event.PaymentCapturedEvent;
import com.micarch.ddd.payment.event.PaymentFailedEvent;
import com.micarch.ddd.shared.vo.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Application Service for the Order bounded context.
 * Orchestrates domain logic, persistence, and domain event publishing.
 * Has NO knowledge of Inventory or Payment internals — only reacts to events.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final String ORDER_TOPIC = "order-events";

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderService(OrderRepository orderRepository,
                        KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // ── Commands ─────────────────────────────────────────────────────

    @Transactional
    public Order placeOrder(String customerId, List<OrderLineItem> lineItems, ShippingAddress address) {
        var order = Order.create(customerId, lineItems, address);
        orderRepository.save(order);

        // Publish domain event for cross-context communication
        var eventItems = order.getLineItems().stream()
                .map(li -> new OrderPlacedEvent.LineItemDetail(
                        li.getProductId(), li.getProductName(), li.getQuantity()))
                .toList();

        var event = new OrderPlacedEvent(
                order.getOrderId(),
                order.getCustomerId(),
                eventItems,
                order.getTotalAmount().getAmount(),
                order.getTotalAmount().getCurrency());

        kafkaTemplate.send(ORDER_TOPIC, order.getOrderId(), event);
        log.info("📦 Order placed: {} for customer {} — total: {}",
                order.getOrderId(), customerId, order.getTotalAmount());

        return order;
    }

    @Transactional
    public Order cancelOrder(String orderId) {
        var order = findById(orderId);
        order.cancel();
        orderRepository.save(order);

        var cancelItems = order.getLineItems().stream()
                .map(li -> new OrderCancelledEvent.LineItemDetail(li.getProductId(), li.getQuantity()))
                .toList();
        kafkaTemplate.send(ORDER_TOPIC, orderId, new OrderCancelledEvent(orderId, cancelItems));

        log.info("❌ Order cancelled: {}", orderId);
        return order;
    }

    // ── Queries ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Order findById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    // ── Event Listeners (reacting to other bounded contexts) ────────

    /**
     * Payment was captured successfully → confirm the order.
     */
    @KafkaListener(topics = "payment-events", groupId = "order-context",
            properties = "spring.json.value.default.type=com.micarch.ddd.payment.event.PaymentCapturedEvent")
    public void onPaymentCaptured(PaymentCapturedEvent event) {
        log.info("💳 Received PaymentCapturedEvent for order: {}", event.orderId());
        try {
            var order = findById(event.orderId());
            order.confirm();
            orderRepository.save(order);

            kafkaTemplate.send(ORDER_TOPIC, order.getOrderId(),
                    new OrderConfirmedEvent(order.getOrderId(), order.getCustomerId()));

            log.info("✅ Order auto-confirmed: {}", order.getOrderId());
        } catch (Exception e) {
            log.error("Failed to confirm order {}: {}", event.orderId(), e.getMessage());
        }
    }

    /**
     * Payment failed → cancel the order.
     */
    @KafkaListener(topics = "payment-events", groupId = "order-payment-fail",
            properties = "spring.json.value.default.type=com.micarch.ddd.payment.event.PaymentFailedEvent")
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.info("💳❌ Received PaymentFailedEvent for order: {}", event.orderId());
        try {
            cancelOrder(event.orderId());
        } catch (Exception e) {
            log.error("Failed to cancel order {}: {}", event.orderId(), e.getMessage());
        }
    }

    /**
     * Stock reservation failed → cancel the order.
     */
    @KafkaListener(topics = "inventory-events", groupId = "order-stock-fail",
            properties = "spring.json.value.default.type=com.micarch.ddd.inventory.event.StockReservationFailedEvent")
    public void onStockReservationFailed(StockReservationFailedEvent event) {
        log.info("📦❌ Received StockReservationFailedEvent for order: {}", event.orderId());
        try {
            var order = findById(event.orderId());
            order.cancel();
            orderRepository.save(order);
            log.info("❌ Order auto-cancelled due to stock failure: {}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to cancel order {}: {}", event.orderId(), e.getMessage());
        }
    }
}
