package com.micarch.cqrs.query.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * QUERY SIDE: De-normalized read model stored in MongoDB. Built by the
 * projection handler consuming Kafka events. Optimized for reads — no joins
 * needed.
 */
@Document(collection = "order_views")
public class OrderView {

    @Id
    private String id;       // MongoDB document ID

    @Indexed
    private Long orderId;    // matches write-side PK

    @Indexed
    private String customerId;

    private String productId;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String state;
    private Instant lastUpdated;

    public OrderView() {
    }

    public OrderView(Long orderId, String customerId, String productId,
            Integer quantity, BigDecimal totalPrice, String state) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.state = state;
        this.lastUpdated = Instant.now();
    }

    public void updateState(String newState) {
        this.state = newState;
        this.lastUpdated = Instant.now();
    }

    // Getters
    public String getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
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

    public String getState() {
        return state;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }
}
