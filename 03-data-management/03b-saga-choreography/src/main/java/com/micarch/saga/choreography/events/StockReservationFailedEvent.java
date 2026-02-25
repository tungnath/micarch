package com.micarch.saga.choreography.events;

/**
 * Compensating event published by Inventory service when stock reservation
 * fails. The Order service listens for this to cancel the order (saga
 * rollback).
 */
public record StockReservationFailedEvent(
        String sagaId,
        String orderId,
        String reason
        ) {

}
