package com.micarch.eventsourcing.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.micarch.eventsourcing.command.OrderCommands.*;
import com.micarch.eventsourcing.command.OrderCommands.CancelOrderCommand;
import com.micarch.eventsourcing.command.OrderCommands.ConfirmOrderCommand;
import com.micarch.eventsourcing.command.OrderCommands.PlaceOrderCommand;
import com.micarch.eventsourcing.projection.OrderProjection.OrderSummary;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Event Sourcing", description = "Commands via Axon Gateway → EventStore. Queries from H2 projection.")
public class OrderController {

    private final CommandGateway commandGateway;
    private final JpaRepository<OrderSummary, String> summaryRepository;

    public OrderController(CommandGateway commandGateway,
            JpaRepository<OrderSummary, String> summaryRepository) {
        this.commandGateway = commandGateway;
        this.summaryRepository = summaryRepository;
    }

    public record PlaceOrderRequest(String customerId, String productId, int quantity, BigDecimal unitPrice) {

    }

    @PostMapping
    @Operation(summary = "Place an order — stored as event in Axon Server")
    public CompletableFuture<String> placeOrder(@RequestBody PlaceOrderRequest req) {
        String orderId = UUID.randomUUID().toString();
        return commandGateway.send(new PlaceOrderCommand(orderId, req.customerId(), req.productId(), req.quantity(), req.unitPrice()))
                .thenApply(r -> orderId);
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "Confirm order — applies OrderConfirmedEvent")
    public CompletableFuture<Void> confirm(@PathVariable String id) {
        return commandGateway.send(new ConfirmOrderCommand(id));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel order — applies OrderCancelledEvent")
    public CompletableFuture<Void> cancel(@PathVariable String id) {
        return commandGateway.send(new CancelOrderCommand(id));
    }

    @GetMapping
    @Operation(summary = "Query all orders from the H2 read projection")
    public ResponseEntity<List<OrderSummary>> listAll() {
        return ResponseEntity.ok(summaryRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Query a single order from the projection by ID")
    public ResponseEntity<OrderSummary> getById(@PathVariable String id) {
        return summaryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
