package com.micarch.eventsourcing.command;

import java.math.BigDecimal;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

// Commands — sent to the aggregate; Axon routes them by @TargetAggregateIdentifier
public class OrderCommands {

    public record PlaceOrderCommand(
            @TargetAggregateIdentifier String orderId,
            String customerId,
            String productId,
            int quantity,
            BigDecimal unitPrice
            ) {

    }

    public record ConfirmOrderCommand(
            @TargetAggregateIdentifier String orderId
            ) {

    }

    public record CancelOrderCommand(
            @TargetAggregateIdentifier String orderId
            ) {

    }
}
