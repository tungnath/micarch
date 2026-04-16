package com.micarch.ddd.order.controller;

import com.micarch.ddd.order.domain.Order;
import com.micarch.ddd.order.domain.OrderLineItem;
import com.micarch.ddd.order.domain.ShippingAddress;
import com.micarch.ddd.order.service.OrderService;
import com.micarch.ddd.shared.vo.Money;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for the Order bounded context. Exposes ONLY Order domain
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
    @Operation(summary = "Place a new order",
            description = "Creates order with line items and publishes OrderPlacedEvent to Kafka")
    public Order placeOrder(@RequestBody PlaceOrderRequest req) {
        var lineItems = req.items().stream()
                .map(item -> new OrderLineItem(
                item.productId(),
                item.productName(),
                item.quantity(),
                Money.inr(item.unitPrice())))
                .toList();

        var address = new ShippingAddress(
                req.shippingAddress().street(),
                req.shippingAddress().city(),
                req.shippingAddress().state(),
                req.shippingAddress().zipCode(),
                req.shippingAddress().country());

        return orderService.placeOrder(req.customerId(), lineItems, address);
    }

    @GetMapping
    @Operation(summary = "List all orders")
    public List<Order> listOrders() {
        return orderService.findAll();
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    public Order getOrder(@PathVariable("orderId") String orderId) {
        return orderService.findById(orderId);
    }

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel an order", description = "Publishes OrderCancelledEvent for stock release")
    public Order cancelOrder(@PathVariable("orderId") String orderId) {
        return orderService.cancelOrder(orderId);
    }

    // ── Request DTOs — live in the controller layer, NOT in the domain ──
    public record PlaceOrderRequest(
            String customerId,
            List<LineItemRequest> items,
            AddressRequest shippingAddress
            ) {

    }

    public record LineItemRequest(
            String productId,
            String productName,
            int quantity,
            BigDecimal unitPrice
            ) {

    }

    public record AddressRequest(
            String street,
            String city,
            String state,
            String zipCode,
            String country
            ) {

    }
}
