package com.micarch.ddd.payment.domain;

import com.micarch.ddd.shared.vo.Money;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * PAYMENT — Aggregate Root of the Payment bounded context.
 *
 * Manages payment lifecycle for an order. Enforces invariants:
 * - Only INITIATED payments can be captured or failed
 * - Only CAPTURED payments can be refunded
 * - Payment is always tied to an orderId (from Order context)
 *
 * Note: orderId is just a string reference — NOT a foreign key to Order table.
 * This is intentional DDD boundary isolation.
 */
@Entity
@Table(name = "payments", schema = "payment_ctx")
public class Payment {

    @Id
    private String paymentId;

    @Column(nullable = false)
    private String orderId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "payment_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "payment_currency"))
    })
    private Money amount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "payment_type")),
            @AttributeOverride(name = "last4Digits", column = @Column(name = "payment_last4"))
    })
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    private String failureReason;

    protected Payment() {
    }

    // ── Factory Method ──────────────────────────────────────────────

    public static Payment initiate(String orderId, Money amount, PaymentMethod method) {
        if (orderId == null || orderId.isBlank()) throw new IllegalArgumentException("Order ID is required");
        if (amount == null) throw new IllegalArgumentException("Amount is required");
        if (method == null) throw new IllegalArgumentException("Payment method is required");

        var payment = new Payment();
        payment.paymentId = UUID.randomUUID().toString();
        payment.orderId = orderId;
        payment.amount = amount;
        payment.paymentMethod = method;
        payment.status = PaymentStatus.INITIATED;
        payment.createdAt = Instant.now();
        payment.updatedAt = Instant.now();
        return payment;
    }

    // ── Domain Behavior ─────────────────────────────────────────────

    /**
     * Capture payment — simulates successful payment processing.
     */
    public void capture() {
        if (this.status != PaymentStatus.INITIATED) {
            throw new IllegalStateException("Only INITIATED payments can be captured, current: " + this.status);
        }
        this.status = PaymentStatus.CAPTURED;
        this.updatedAt = Instant.now();
    }

    /**
     * Mark payment as failed.
     */
    public void fail(String reason) {
        if (this.status != PaymentStatus.INITIATED) {
            throw new IllegalStateException("Only INITIATED payments can fail, current: " + this.status);
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
    }

    /**
     * Refund a captured payment.
     */
    public void refund() {
        if (this.status != PaymentStatus.CAPTURED) {
            throw new IllegalStateException("Only CAPTURED payments can be refunded, current: " + this.status);
        }
        this.status = PaymentStatus.REFUNDED;
        this.updatedAt = Instant.now();
    }

    // ── Getters ─────────────────────────────────────────────────────

    public String getPaymentId() { return paymentId; }
    public String getOrderId() { return orderId; }
    public Money getAmount() { return amount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public PaymentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getFailureReason() { return failureReason; }
}
