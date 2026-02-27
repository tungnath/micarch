package com.micarch.eventsourcing.aggregate;

import java.math.BigDecimal;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.micarch.eventsourcing.command.OrderCommands.CancelOrderCommand;
import com.micarch.eventsourcing.command.OrderCommands.ConfirmOrderCommand;
import com.micarch.eventsourcing.command.OrderCommands.PlaceOrderCommand;
import com.micarch.eventsourcing.events.OrderEvents.OrderCancelledEvent;
import com.micarch.eventsourcing.events.OrderEvents.OrderConfirmedEvent;
import com.micarch.eventsourcing.events.OrderEvents.OrderPlacedEvent;

/**
 * The core of Event Sourcing.
 *
 * Key rules: 1. @CommandHandler — validates and emits events (no state mutation
 * here!) 2. @EventSourcingHandler — applies events to mutate state (replayed on
 * load) 3. State is rebuilt by replaying all stored events from the event store
 * 4. AggregateLifecycle.apply() stores the event AND triggers the handler
 */
@Aggregate
public class OrderAggregate {

    private static final Logger log = LoggerFactory.getLogger(OrderAggregate.class);

    @AggregateIdentifier
    private String orderId;
    private String customerId;
    private String productId;
    private int quantity;
    private BigDecimal unitPrice;
    private String state;

    // Required no-arg constructor for Axon event sourcing reconstruction
    protected OrderAggregate() {
    }

    // ─── COMMAND HANDLERS ──────────────────────────────────────────────────────
    @CommandHandler
    public OrderAggregate(PlaceOrderCommand cmd) {
        log.info("Handling PlaceOrderCommand for orderId={}", cmd.orderId());
        // Validate, then emit — never mutate state directly
        AggregateLifecycle.apply(new OrderPlacedEvent(
                cmd.orderId(), cmd.customerId(), cmd.productId(), cmd.quantity(), cmd.unitPrice()
        ));
    }

    @CommandHandler
    public void handle(ConfirmOrderCommand cmd) {
        log.info("Handling ConfirmOrderCommand for orderId={}", cmd.orderId());
        if (!"PENDING".equals(state)) {
            throw new IllegalStateException("Only PENDING orders can be confirmed");
        }
        AggregateLifecycle.apply(new OrderConfirmedEvent(cmd.orderId()));
    }

    @CommandHandler
    public void handle(CancelOrderCommand cmd) {
        log.info("Handling CancelOrderCommand for orderId={}", cmd.orderId());
        if ("SHIPPED".equals(state)) {
            throw new IllegalStateException("Shipped orders cannot be cancelled");
        }
        AggregateLifecycle.apply(new OrderCancelledEvent(cmd.orderId(), "Customer request"));
    }

    // ─── EVENT SOURCING HANDLERS (state reconstruction from events) ────────────
    @EventSourcingHandler
    public void on(OrderPlacedEvent event) {
        this.orderId = event.orderId();
        this.customerId = event.customerId();
        this.productId = event.productId();
        this.quantity = event.quantity();
        this.unitPrice = event.unitPrice();
        this.state = "PENDING";
    }

    @EventSourcingHandler
    public void on(OrderConfirmedEvent event) {
        this.state = "CONFIRMED";
    }

    @EventSourcingHandler
    public void on(OrderCancelledEvent event) {
        this.state = "CANCELLED";
    }

    // Getters for projection use
    public String getOrderId() {
        return orderId;
    }

    public String getState() {
        return state;
    }
}
