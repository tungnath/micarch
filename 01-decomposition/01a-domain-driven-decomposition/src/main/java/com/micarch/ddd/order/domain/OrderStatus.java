package com.micarch.ddd.order.domain;

/**
 * Order status — part of the Order bounded context's ubiquitous language.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
