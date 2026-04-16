package com.micarch.ddd.order.domain;

import com.micarch.ddd.shared.vo.Money;
import jakarta.persistence.*;
import java.util.Objects;

/**
 * Order Line Item — Entity owned by the Order aggregate.
 * Cannot exist independently — always belongs to an Order.
 * Identity is local to the aggregate (not globally unique).
 */
@Entity
@Table(name = "order_line_items", schema = "order_ctx")
public class OrderLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_price_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "unit_price_currency"))
    })
    private Money unitPrice;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "line_total_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "line_total_currency"))
    })
    private Money lineTotal;

    protected OrderLineItem() {
    }

    public OrderLineItem(String productId, String productName, int quantity, Money unitPrice) {
        if (productId == null || productId.isBlank()) throw new IllegalArgumentException("Product ID is required");
        if (productName == null || productName.isBlank()) throw new IllegalArgumentException("Product name is required");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (unitPrice == null) throw new IllegalArgumentException("Unit price is required");

        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = unitPrice.multiply(quantity);
    }

    // ── Getters ─────────────────────────────────────────────────────
    public Long getId() { return id; }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public Money getUnitPrice() { return unitPrice; }
    public Money getLineTotal() { return lineTotal; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderLineItem that = (OrderLineItem) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
