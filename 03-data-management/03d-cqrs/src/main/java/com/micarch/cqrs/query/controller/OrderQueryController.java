package com.micarch.cqrs.query.controller;

import com.micarch.cqrs.query.model.OrderView;
import com.micarch.cqrs.query.repository.OrderViewRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * QUERY side API: handles all read operations from MongoDB. No writes happen
 * here. Eventually consistent with MongoDB projection.
 */
@RestController
@RequestMapping("/api/queries/orders")
@RequiredArgsConstructor
@Tag(name = "Order Queries", description = "CQRS Read Side — MongoDB projection (eventually consistent)")
public class OrderQueryController {

    private final OrderViewRepository viewRepository;

    @GetMapping
    @Operation(summary = "List all orders from read model (MongoDB)")
    public ResponseEntity<List<OrderView>> listAll() {
        return ResponseEntity.ok(viewRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by write-side ID (from read model)")
    public ResponseEntity<OrderView> getById(@PathVariable Long id) {
        return viewRepository.findByOrderId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all orders for a customer")
    public ResponseEntity<List<OrderView>> byCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(viewRepository.findByCustomerId(customerId));
    }

    @GetMapping("/state/{state}")
    @Operation(summary = "Get orders by state")
    public ResponseEntity<List<OrderView>> byState(@PathVariable String state) {
        return ResponseEntity.ok(viewRepository.findByState(state.toUpperCase()));
    }
}
