package com.micarch.cqrs.command.domain;

public enum OrderState {
    PENDING,
    CONFIRMED,
    SHIPPED,
    CANCELLED
}
