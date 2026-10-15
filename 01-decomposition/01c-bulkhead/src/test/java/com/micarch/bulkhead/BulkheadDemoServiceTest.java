package com.micarch.bulkhead;

import com.micarch.bulkhead.service.BulkheadDemoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BulkheadDemoServiceTest {

    @Autowired
    private BulkheadDemoService service;

    @Test
    void orderSemaphoreRejectsWorkWhenPartitionIsFull() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<java.util.concurrent.Future<BulkheadDemoService.BulkheadResponse>> futures = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                int request = i;
                futures.add(executor.submit(() -> service.processOrder("order-" + request, 900)
                        .getBody()));
            }

            var responses = new ArrayList<BulkheadDemoService.BulkheadResponse>();
            for (var future : futures) {
                responses.add(future.get());
            }

            assertThat(responses).anyMatch(response -> "REJECTED".equals(response.status()));
            assertThat(responses).anyMatch(response -> "COMPLETED".equals(response.status()));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void inventoryUsesDedicatedThreadPool() throws Exception {
        var response = service.processInventory("SKU-1", 10).toCompletableFuture().get();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().status()).isEqualTo("COMPLETED");
        assertThat(response.getBody().workerThread()).contains("inventoryService");
    }
}
