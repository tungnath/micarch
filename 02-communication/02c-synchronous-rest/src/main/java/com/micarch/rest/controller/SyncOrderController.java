package com.micarch.rest.controller;

import java.math.BigDecimal;

import com.micarch.rest.domain.SyncOrderService;
import com.micarch.rest.domain.SyncOrderService.OrderResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class SyncOrderController {

    private final SyncOrderService orderService;

    public SyncOrderController(SyncOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResult> placeOrder(@RequestBody PlaceOrderRequest request) {
        OrderResult result = orderService.placeOrder(request.productId(), request.quantity(), request.unitPrice());
        return result.status().equals("CONFIRMED")
                ? ResponseEntity.status(HttpStatus.CREATED).body(result)
                : ResponseEntity.status(HttpStatus.CONFLICT).body(result);
    }

    public record PlaceOrderRequest(String productId, int quantity, BigDecimal unitPrice) {
    }
}
