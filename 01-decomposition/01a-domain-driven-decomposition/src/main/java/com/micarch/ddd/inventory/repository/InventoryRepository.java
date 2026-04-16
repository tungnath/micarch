package com.micarch.ddd.inventory.repository;

import com.micarch.ddd.inventory.domain.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Inventory repository — scoped to the Inventory bounded context ONLY.
 */
public interface InventoryRepository extends JpaRepository<InventoryItem, String> {

    @Query("SELECT i FROM InventoryItem i WHERE i.sku = :sku")
    Optional<InventoryItem> findBySku(@Param("sku") String sku);
}
