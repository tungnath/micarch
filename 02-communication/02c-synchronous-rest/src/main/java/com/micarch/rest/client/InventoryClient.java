package com.micarch.rest.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory-service", url = "${services.inventory-service-url:http://localhost:8301}")
public interface InventoryClient {

    @GetMapping("/api/inventory/{productId}")
    StockResponse checkStock(@PathVariable("productId") String productId);

    @PutMapping("/api/inventory/{productId}/reserve")
    void reserveStock(@PathVariable("productId") String productId, @RequestParam("quantity") int quantity);

    record StockResponse(String productId, int availableStock) {
        public boolean available(int quantity) {
            return availableStock >= quantity;
        }
    }
}
