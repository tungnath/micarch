package com.micarch.ddd.inventory.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * INVENTORY ITEM — Aggregate Root of the Inventory bounded context.
 *
 * Manages stock levels for a product. Enforces invariants:
 * - Available stock cannot go negative
 * - Reservations are tracked separately from available stock
 * - Stock can only be released if it was previously reserved
 */
@Entity
@Table(name = "inventory_items", schema = "inventory_ctx")
public class InventoryItem {

    @Id
    private String itemId;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private int availableStock;

    @Column(nullable = false)
    private int reservedStock;

    protected InventoryItem() {
    }

    // ── Factory Method ──────────────────────────────────────────────

    public static InventoryItem create(String sku, String productName, int initialStock) {
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("SKU is required");
        if (productName == null || productName.isBlank()) throw new IllegalArgumentException("Product name is required");
        if (initialStock < 0) throw new IllegalArgumentException("Initial stock cannot be negative");

        var item = new InventoryItem();
        item.itemId = UUID.randomUUID().toString();
        item.sku = sku;
        item.productName = productName;
        item.availableStock = initialStock;
        item.reservedStock = 0;
        return item;
    }

    // ── Domain Behavior ─────────────────────────────────────────────

    /**
     * Reserve stock for an order. Moves quantity from available to reserved.
     * @throws IllegalStateException if insufficient stock
     */
    public void reserveStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (quantity > availableStock) {
            throw new IllegalStateException(
                    "Insufficient stock for " + sku + ": requested " + quantity + ", available " + availableStock);
        }
        this.availableStock -= quantity;
        this.reservedStock += quantity;
    }

    /**
     * Release previously reserved stock back to available (e.g., order cancelled).
     */
    public void releaseStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (quantity > reservedStock) {
            throw new IllegalStateException(
                    "Cannot release more than reserved for " + sku + ": requested " + quantity + ", reserved " + reservedStock);
        }
        this.reservedStock -= quantity;
        this.availableStock += quantity;
    }

    /**
     * Confirm stock deduction — removes from reserved (order shipped/delivered).
     */
    public void confirmReduction(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (quantity > reservedStock) {
            throw new IllegalStateException("Cannot confirm more than reserved for " + sku);
        }
        this.reservedStock -= quantity;
    }

    /**
     * Add stock (replenishment).
     */
    public void addStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        this.availableStock += quantity;
    }

    public boolean hasAvailableStock(int quantity) {
        return this.availableStock >= quantity;
    }

    // ── Getters ─────────────────────────────────────────────────────

    public String getItemId() { return itemId; }
    public String getSku() { return sku; }
    public String getProductName() { return productName; }
    public int getAvailableStock() { return availableStock; }
    public int getReservedStock() { return reservedStock; }
    public int getTotalStock() { return availableStock + reservedStock; }
}
