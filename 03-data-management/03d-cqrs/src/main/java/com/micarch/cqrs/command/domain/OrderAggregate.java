package com.micarch.cqrs.command.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * COMMAND SIDE: The write model — normalized, validates invariants. Only
 * mutated via explicit command methods.
 */
@Entity
@Table(name = "orders")
public class OrderAggregate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderState state;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    protected OrderAggregate() {
    }

    public static OrderAggregate create(String customerId, String productId, int quantity, BigDecimal price) {
        OrderAggregate order = new OrderAggregate();
        order.customerId = customerId;
        order.productId = productId;
        order.quantity = quantity;
        order.totalPrice = price.multiply(BigDecimal.valueOf(quantity));
        order.state = OrderState.PENDING;
        order.createdAt = Instant.now();
        order.updatedAt = Instant.now();
        return order;
    }

    public void confirm() {
        if (state != OrderState.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be confirmed");
        }
        this.state = OrderState.CONFIRMED;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        if (state == OrderState.SHIPPED) {
            throw new IllegalStateException("Shipped orders cannot be cancelled");
        }
        this.state = OrderState.CANCELLED;
        this.updatedAt = Instant.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public OrderState getState() {
        return state;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
