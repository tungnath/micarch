package com.micarch.ddd.config;

import com.micarch.ddd.inventory.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds initial sample data on application startup.
 * Uses domain services (not raw SQL) — proper DDD approach.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final InventoryService inventoryService;

    public DataSeeder(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("🌱 Seeding sample inventory data...");

        // Electronics
        inventoryService.addInventory("LAPTOP-001", "MacBook Pro 14 M3", 50);
        inventoryService.addInventory("LAPTOP-002", "Dell XPS 15", 35);
        inventoryService.addInventory("PHONE-001", "iPhone 15 Pro", 100);
        inventoryService.addInventory("PHONE-002", "Samsung Galaxy S24 Ultra", 80);

        // Accessories
        inventoryService.addInventory("MOUSE-001", "Logitech MX Master 3S", 200);
        inventoryService.addInventory("KEYBOARD-001", "Keychron K2 Pro", 150);
        inventoryService.addInventory("HEADPHONE-001", "Sony WH-1000XM5", 75);
        inventoryService.addInventory("CHARGER-001", "Anker 65W GaN Charger", 300);

        // Wearables
        inventoryService.addInventory("WATCH-001", "Apple Watch Series 9", 60);
        inventoryService.addInventory("WATCH-002", "Samsung Galaxy Watch 6", 45);

        log.info("✅ Sample inventory seeded — {} items", 10);
    }
}
