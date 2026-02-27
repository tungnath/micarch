package com.micarch.eventsourcing.projection;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.micarch.eventsourcing.events.OrderEvents.OrderCancelledEvent;
import com.micarch.eventsourcing.events.OrderEvents.OrderConfirmedEvent;
import com.micarch.eventsourcing.events.OrderEvents.OrderPlacedEvent;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Read model (projection) updated by @EventHandler. Stored in H2 — a flat,
 * de-normalized view of orders. Can be rebuilt at any time by replaying events
 * from Axon Server.
 */
@Component
public class OrderProjection {

    private final OrderSummaryRepository repository;

    public OrderProjection(OrderSummaryRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(OrderPlacedEvent event) {
        OrderSummary summary = new OrderSummary(
                event.orderId(), event.customerId(), event.productId(),
                event.quantity(), event.unitPrice(), "PENDING"
        );
        repository.save(summary);
    }

    @EventHandler
    public void on(OrderConfirmedEvent event) {
        repository.findById(event.orderId()).ifPresent(s -> {
            s.setState("CONFIRMED");
            repository.save(s);
        });
    }

    @EventHandler
    public void on(OrderCancelledEvent event) {
        repository.findById(event.orderId()).ifPresent(s -> {
            s.setState("CANCELLED");
            repository.save(s);
        });
    }

    // ─── Embedded JPA entity for the read model ───────────────────────────────
    @Entity
    @Table(name = "order_summaries")
    public static class OrderSummary {

        @Id
        private String orderId;
        private String customerId;
        private String productId;
        private int quantity;
        private BigDecimal unitPrice;
        private String state;
        private Instant updatedAt;

        protected OrderSummary() {
        }

        public OrderSummary(String orderId, String customerId, String productId,
                int quantity, BigDecimal unitPrice, String state) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.state = state;
            this.updatedAt = Instant.now();
        }

        public void setState(String state) {
            this.state = state;
            this.updatedAt = Instant.now();
        }

        public String getOrderId() {
            return orderId;
        }

        public String getCustomerId() {
            return customerId;
        }

        public String getProductId() {
            return productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public String getState() {
            return state;
        }

        public Instant getUpdatedAt() {
            return updatedAt;
        }
    }

    @Repository
    interface OrderSummaryRepository extends JpaRepository<OrderSummary, String> {

        List<OrderSummary> findByCustomerId(String customerId);
    }
}
