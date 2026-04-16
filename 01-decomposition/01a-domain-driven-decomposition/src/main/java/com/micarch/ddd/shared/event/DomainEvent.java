package com.micarch.ddd.shared.event;

/**
 * Marker interface for all domain events across bounded contexts.
 * This is the ONLY shared type — keeps the shared kernel minimal.
 */
public interface DomainEvent {
}
