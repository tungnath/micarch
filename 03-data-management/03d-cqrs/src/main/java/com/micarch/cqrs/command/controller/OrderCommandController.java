package com.micarch.cqrs.command.controller;

import com.micarch.cqrs.command.domain.OrderAggregate;
import com.micarch.cqrs.command.service.OrderCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * COMMAND side API: handles all write operations. Returns 202 Accepted — the
 * query side is eventually consistent.
 */
@RestController
@RequestMapping("/api/commands/orders")
@RequiredArgsConstructor
@Tag(name = "Order Commands", description = "CQRS Write Side — Postgres + Kafka event publishing")
public class OrderCommandController {

    private final OrderCommandService commandService;

    public record PlaceOrderRequest(String customerId, String productId, int quantity, BigDecimal unitPrice) {

    }

    @PostMapping
    @Operation(summary = "Place a new order",
            description = "Writes to Postgres, publishes ORDER_PLACED event to Kafka. Read model updates asynchronously.")
    public ResponseEntity<OrderAggregate> placeOrder(@RequestBody PlaceOrderRequest req) {
        OrderAggregate order = commandService.placeOrder(
                req.customerId(), req.productId(), req.quantity(), req.unitPrice());
        return ResponseEntity.accepted().body(order);
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "Confirm an order")
    public ResponseEntity<OrderAggregate> confirm(@PathVariable("id") Long id) {
        return ResponseEntity.accepted().body(commandService.confirmOrder(id));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<OrderAggregate> cancel(@PathVariable("id") Long id) {
        return ResponseEntity.accepted().body(commandService.cancelOrder(id));
    }
}
