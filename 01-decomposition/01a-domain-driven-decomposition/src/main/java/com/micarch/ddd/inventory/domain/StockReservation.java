package com.micarch.ddd.inventory.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Stock Reservation — tracks stock reserved against an order.
 * Enables releasing stock when an order is cancelled.
 */
@Entity
@Table(name = "stock_reservations", schema = "inventory_ctx")
public class StockReservation {

    @Id
    private String reservationId;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    protected StockReservation() {
    }

    public static StockReservation create(String orderId, String sku, int quantity) {
        var reservation = new StockReservation();
        reservation.reservationId = UUID.randomUUID().toString();
        reservation.orderId = orderId;
        reservation.sku = sku;
        reservation.quantity = quantity;
        reservation.status = ReservationStatus.RESERVED;
        reservation.createdAt = Instant.now();
        return reservation;
    }

    public void release() {
        if (this.status != ReservationStatus.RESERVED) {
            throw new IllegalStateException("Can only release RESERVED reservations");
        }
        this.status = ReservationStatus.RELEASED;
    }

    public void confirm() {
        if (this.status != ReservationStatus.RESERVED) {
            throw new IllegalStateException("Can only confirm RESERVED reservations");
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    // ── Getters ─────────────────────────────────────────────────────

    public String getReservationId() { return reservationId; }
    public String getOrderId() { return orderId; }
    public String getSku() { return sku; }
    public int getQuantity() { return quantity; }
    public ReservationStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public enum ReservationStatus {
        RESERVED, CONFIRMED, RELEASED
    }
}
