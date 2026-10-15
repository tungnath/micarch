package com.micarch.bulkhead.controller;

import com.micarch.bulkhead.service.BulkheadDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletionStage;

@RestController
@RequestMapping("/api/bulkheads")
@Tag(name = "Bulkheads", description = "Semaphore and thread-pool isolation demonstrations")
public class BulkheadController {

    private final BulkheadDemoService service;

    public BulkheadController(BulkheadDemoService service) {
        this.service = service;
    }

    @GetMapping("/orders/{orderId}")
    @Operation(summary = "Execute work through the order semaphore bulkhead")
    public ResponseEntity<BulkheadDemoService.BulkheadResponse> order(
            @PathVariable("orderId") String orderId,
            @RequestParam(name = "delayMs", defaultValue = "1000") long delayMs) {
        return service.processOrder(orderId, delayMs);
    }

    @GetMapping("/payments/{paymentId}")
    @Operation(summary = "Execute work through the payment semaphore bulkhead")
    public ResponseEntity<BulkheadDemoService.BulkheadResponse> payment(
            @PathVariable("paymentId") String paymentId,
            @RequestParam(name = "delayMs", defaultValue = "1000") long delayMs) {
        return service.processPayment(paymentId, delayMs);
    }

    @GetMapping("/inventory/{sku}")
    @Operation(summary = "Execute work through the inventory thread-pool bulkhead")
    public CompletionStage<ResponseEntity<BulkheadDemoService.BulkheadResponse>> inventory(
            @PathVariable("sku") String sku,
            @RequestParam(name = "delayMs", defaultValue = "1000") long delayMs) {
        return service.processInventory(sku, delayMs);
    }
}
