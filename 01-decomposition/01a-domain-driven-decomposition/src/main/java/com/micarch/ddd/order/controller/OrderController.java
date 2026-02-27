package com.micarch.ddd.order.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.micarch.ddd.order.domain.Order;
import com.micarch.ddd.order.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller for the Order bounded context. Only exposes Order domain
 * concepts — no cross-context leakage.
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Service", description = "Order Bounded Context API")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Place a new order", description = "Creates order and publishes OrderPlacedEvent to Kafka")
    public Order placeOrder(@RequestBody PlaceOrderRequest req) {
        return orderService.placeOrder(req.customerId(), req.productId(), req.quantity(), req.unitPrice());
    }

    @GetMapping
    @Operation(summary = "List all orders")
    public List<Order> listOrders() {
        return orderService.findAll();
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    public Order getOrder(@PathVariable String orderId) {
        return orderService.findById(orderId);
    }

    @PutMapping("/{orderId}/confirm")
    @Operation(summary = "Confirm a pending order")
    public Order confirmOrder(@PathVariable String orderId) {
        return orderService.confirmOrder(orderId);
    }

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel an order")
    public Order cancelOrder(@PathVariable String orderId) {
        return orderService.cancelOrder(orderId);
    }

    // Request DTO — lives in the controller, not the domain
    public record PlaceOrderRequest(String customerId, String productId, int quantity, BigDecimal unitPrice) {
    }
}
