package com.micarch.ddd.order.domain;

import com.micarch.ddd.shared.vo.Money;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * ORDER — Aggregate Root.
 *
 * This is the central aggregate of the Order bounded context. It enforces ALL
 * business invariants for orders:
 * - An order must have at least one line item
 * - Only PENDING orders can be confirmed
 * - Shipped orders cannot be cancelled
 *
 * Cross-context communication happens ONLY via domain events — never via
 * direct references to Inventory or Payment entities.
 */
@Entity
@Table(name = "orders", schema = "order_ctx")
public class Order {

    @Id
    private String orderId;

    @Column(nullable = false)
    private String customerId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    private List<OrderLineItem> lineItems = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "shipping_street")),
            @AttributeOverride(name = "city", column = @Column(name = "shipping_city")),
            @AttributeOverride(name = "state", column = @Column(name = "shipping_state")),
            @AttributeOverride(name = "zipCode", column = @Column(name = "shipping_zip")),
            @AttributeOverride(name = "country", column = @Column(name = "shipping_country"))
    })
    private ShippingAddress shippingAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "total_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "total_currency"))
    })
    private Money totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    protected Order() {
    }

    // ── Factory Method — the ONLY way to create an Order ────────────
    public static Order create(String customerId, List<OrderLineItem> items, ShippingAddress address) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one line item");
        }
        if (address == null) {
            throw new IllegalArgumentException("Shipping address is required");
        }

        var order = new Order();
        order.orderId = UUID.randomUUID().toString();
        order.customerId = customerId;
        order.lineItems = new ArrayList<>(items);
        order.shippingAddress = address;
        order.totalAmount = order.calculateTotal();
        order.status = OrderStatus.PENDING;
        order.createdAt = Instant.now();
        order.updatedAt = Instant.now();
        return order;
    }

    // ── Domain Behavior ─────────────────────────────────────────────

    /**
     * Confirm an order — only allowed from PENDING status.
     * Typically triggered when PaymentCapturedEvent is received.
     */
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be confirmed, current: " + this.status);
        }
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = Instant.now();
    }

    /**
     * Cancel an order — not allowed if already SHIPPED or DELIVERED.
     * Triggers stock release in the Inventory context via domain event.
     */
    public void cancel() {
        if (this.status == OrderStatus.SHIPPED || this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel a " + this.status + " order");
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    /**
     * Mark order as shipped — only CONFIRMED orders can be shipped.
     */
    public void ship() {
        if (this.status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Only CONFIRMED orders can be shipped, current: " + this.status);
        }
        this.status = OrderStatus.SHIPPED;
        this.updatedAt = Instant.now();
    }

    /**
     * Mark order as delivered — only SHIPPED orders can be delivered.
     */
    public void deliver() {
        if (this.status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Only SHIPPED orders can be delivered, current: " + this.status);
        }
        this.status = OrderStatus.DELIVERED;
        this.updatedAt = Instant.now();
    }

    // ── Private helpers ─────────────────────────────────────────────

    private Money calculateTotal() {
        return lineItems.stream()
                .map(OrderLineItem::getLineTotal)
                .reduce(Money::add)
                .orElse(Money.inr(java.math.BigDecimal.ZERO));
    }

    // ── Getters (no setters — aggregate state is changed only via behavior methods) ──

    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public List<OrderLineItem> getLineItems() { return Collections.unmodifiableList(lineItems); }
    public ShippingAddress getShippingAddress() { return shippingAddress; }
    public Money getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
