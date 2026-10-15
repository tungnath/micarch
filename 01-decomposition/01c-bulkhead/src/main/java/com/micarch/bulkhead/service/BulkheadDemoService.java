package com.micarch.bulkhead.service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static io.github.resilience4j.bulkhead.annotation.Bulkhead.Type.SEMAPHORE;
import static io.github.resilience4j.bulkhead.annotation.Bulkhead.Type.THREADPOOL;

/**
 * Demonstrates two forms of isolation:
 *
 * <ul>
 *     <li>Semaphore bulkheads keep concurrent calls in the request thread bounded.</li>
 *     <li>Thread-pool bulkheads move work to a dedicated executor and isolate its queue.</li>
 * </ul>
 */
@Service
public class BulkheadDemoService {

    @Bulkhead(name = "orderService", type = SEMAPHORE, fallbackMethod = "orderFallback")
    public ResponseEntity<BulkheadResponse> processOrder(String orderId, long delayMs) {
        return execute("order", orderId, delayMs, HttpStatus.OK);
    }

    public ResponseEntity<BulkheadResponse> orderFallback(String orderId, long delayMs, Throwable cause) {
        return rejected("order", orderId, "order semaphore is full", HttpStatus.TOO_MANY_REQUESTS);
    }

    @Bulkhead(name = "paymentService", type = SEMAPHORE, fallbackMethod = "paymentFallback")
    public ResponseEntity<BulkheadResponse> processPayment(String paymentId, long delayMs) {
        return execute("payment", paymentId, delayMs, HttpStatus.OK);
    }

    public ResponseEntity<BulkheadResponse> paymentFallback(String paymentId, long delayMs, Throwable cause) {
        return rejected("payment", paymentId, "payment semaphore is full", HttpStatus.TOO_MANY_REQUESTS);
    }

    @Bulkhead(name = "inventoryService", type = THREADPOOL, fallbackMethod = "inventoryFallback")
    public CompletionStage<ResponseEntity<BulkheadResponse>> processInventory(String sku, long delayMs) {
        // Resilience4j invokes this method on the configured inventory executor.
        // Returning a completed stage preserves that executor boundary instead
        // of accidentally moving the work to the common ForkJoinPool.
        return CompletableFuture.completedFuture(execute(
                "inventory", sku, delayMs, HttpStatus.OK));
    }

    public CompletionStage<ResponseEntity<BulkheadResponse>> inventoryFallback(
            String sku, long delayMs, Throwable cause) {
        return CompletableFuture.completedFuture(rejected(
                "inventory", sku, "inventory thread-pool bulkhead is full", HttpStatus.SERVICE_UNAVAILABLE));
    }

    private ResponseEntity<BulkheadResponse> execute(
            String operation, String requestId, long delayMs, HttpStatus status) {
        var started = Instant.now();
        sleep(delayMs);
        var response = new BulkheadResponse(
                operation,
                requestId,
                "COMPLETED",
                Thread.currentThread().getName(),
                Duration.between(started, Instant.now()).toMillis(),
                null);
        return ResponseEntity.status(status).body(response);
    }

    private ResponseEntity<BulkheadResponse> rejected(
            String operation, String requestId, String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new BulkheadResponse(
                operation,
                requestId,
                "REJECTED",
                Thread.currentThread().getName(),
                0,
                message));
    }

    private void sleep(long delayMs) {
        if (delayMs < 0 || delayMs > 30_000) {
            throw new IllegalArgumentException("delayMs must be between 0 and 30000");
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Bulkhead work was interrupted", ex);
        }
    }

    public record BulkheadResponse(
            String operation,
            String requestId,
            String status,
            String workerThread,
            long elapsedMillis,
            String message) {
    }
}
