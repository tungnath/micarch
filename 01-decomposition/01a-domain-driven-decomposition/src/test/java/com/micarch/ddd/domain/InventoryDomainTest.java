package com.micarch.ddd.domain;

import com.micarch.ddd.inventory.domain.InventoryItem;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryDomainTest {

    @Test
    void reserveAndReleaseMaintainStockInvariant() {
        var item = InventoryItem.create("SKU-1", "Keyboard", 10);

        item.reserveStock(4);
        assertThat(item.getAvailableStock()).isEqualTo(6);
        assertThat(item.getReservedStock()).isEqualTo(4);

        item.releaseStock(4);
        assertThat(item.getAvailableStock()).isEqualTo(10);
        assertThat(item.getReservedStock()).isZero();
    }

    @Test
    void cannotReserveMoreThanAvailableStock() {
        var item = InventoryItem.create("SKU-1", "Keyboard", 2);

        assertThatThrownBy(() -> item.reserveStock(3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient stock");
    }
}
