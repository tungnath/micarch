package com.micarch.ddd.inventory.controller;

import com.micarch.ddd.inventory.domain.InventoryItem;
import com.micarch.ddd.inventory.service.InventoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for the Inventory bounded context. Exposes stock management
 * APIs — isolated from Order/Payment.
 */
@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory Service", description = "Inventory Bounded Context API")
public class InventoryController {

    private final InventoryService inventoryService;
    private final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add or replenish inventory stock")
    public InventoryItem addStock(@RequestBody AddStockRequest req) {
        return inventoryService.addInventory(req.sku(), req.productName(), req.quantity());
    }

    @GetMapping
    @Operation(summary = "List all inventory items")
    public List<InventoryItem> listInventory() {
        return inventoryService.findAll();
    }

    @GetMapping("/{sku}")
    @Operation(summary = "Get inventory item by SKU")
    public InventoryItem getInventoryBySku(@PathVariable("sku") String sku) {
        return inventoryService.findBySku(sku);
    }

    // ── Request DTO ─────────────────────────────────────────────────
    public record AddStockRequest(String sku, String productName, int quantity) {

    }
}
