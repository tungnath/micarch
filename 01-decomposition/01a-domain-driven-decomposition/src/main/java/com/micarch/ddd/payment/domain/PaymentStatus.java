package com.micarch.ddd.payment.domain;

/**
 * Payment status — part of the Payment bounded context's ubiquitous language.
 */
public enum PaymentStatus {
    INITIATED,
    CAPTURED,
    FAILED,
    REFUNDED
}
