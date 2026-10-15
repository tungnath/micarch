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
        inventoryService.seedInventory("LAPTOP-001", "MacBook Pro 14 M3", 50);
        inventoryService.seedInventory("LAPTOP-002", "Dell XPS 15", 35);
        inventoryService.seedInventory("PHONE-001", "iPhone 15 Pro", 100);
        inventoryService.seedInventory("PHONE-002", "Samsung Galaxy S24 Ultra", 80);

        // Accessories
        inventoryService.seedInventory("MOUSE-001", "Logitech MX Master 3S", 200);
        inventoryService.seedInventory("KEYBOARD-001", "Keychron K2 Pro", 150);
        inventoryService.seedInventory("HEADPHONE-001", "Sony WH-1000XM5", 75);
        inventoryService.seedInventory("CHARGER-001", "Anker 65W GaN Charger", 300);

        // Wearables
        inventoryService.seedInventory("WATCH-001", "Apple Watch Series 9", 60);
        inventoryService.seedInventory("WATCH-002", "Samsung Galaxy Watch 6", 45);

        log.info("✅ Sample inventory seeded — {} items", 10);
    }
}
