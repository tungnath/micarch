package com.micarch.ddd.payment.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.Objects;

/**
 * Payment Method — Value Object (immutable).
 * Represents HOW the payment was made, not the payment itself.
 */
@Embeddable
public class PaymentMethod {

    @Enumerated(EnumType.STRING)
    private PaymentType type;

    private String last4Digits;

    protected PaymentMethod() {
    }

    public PaymentMethod(PaymentType type, String last4Digits) {
        if (type == null) throw new IllegalArgumentException("Payment type is required");
        this.type = type;
        this.last4Digits = last4Digits != null ? last4Digits : "****";
    }

    public static PaymentMethod creditCard(String last4) {
        return new PaymentMethod(PaymentType.CREDIT_CARD, last4);
    }

    public static PaymentMethod upi() {
        return new PaymentMethod(PaymentType.UPI, null);
    }

    public static PaymentMethod netBanking() {
        return new PaymentMethod(PaymentType.NET_BANKING, null);
    }

    public PaymentType getType() { return type; }
    public String getLast4Digits() { return last4Digits; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentMethod that = (PaymentMethod) o;
        return type == that.type && Objects.equals(last4Digits, that.last4Digits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, last4Digits);
    }

    @Override
    public String toString() {
        return type + " (****" + last4Digits + ")";
    }

    public enum PaymentType {
        CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING, WALLET
    }
}
