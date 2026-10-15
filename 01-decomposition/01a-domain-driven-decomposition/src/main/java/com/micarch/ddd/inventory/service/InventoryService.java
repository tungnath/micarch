package com.micarch.ddd.inventory.service;

import com.micarch.ddd.inventory.domain.InventoryItem;
import com.micarch.ddd.inventory.domain.StockReservation;
import com.micarch.ddd.inventory.event.StockReleasedEvent;
import com.micarch.ddd.inventory.event.StockReservationFailedEvent;
import com.micarch.ddd.inventory.event.StockReservedEvent;
import com.micarch.ddd.inventory.repository.InventoryRepository;
import com.micarch.ddd.inventory.repository.StockReservationRepository;
import com.micarch.ddd.order.event.OrderCancelledEvent;
import com.micarch.ddd.order.event.OrderPlacedEvent;
import com.micarch.ddd.payment.event.PaymentCapturedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application Service for the Inventory bounded context. Reacts to Order events
 * via Kafka — no direct imports from Order domain.
 */
@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private static final String INVENTORY_TOPIC = "inventory-events";

    private final InventoryRepository inventoryRepository;
    private final StockReservationRepository reservationRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryService(InventoryRepository inventoryRepository,
            StockReservationRepository reservationRepository,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // ── Commands (direct API) ───────────────────────────────────────
    @Transactional
    public InventoryItem addInventory(String sku, String productName, int stock) {
        var existing = inventoryRepository.findBySku(sku);
        if (existing.isPresent()) {
            var item = existing.get();
            item.addStock(stock);
            inventoryRepository.save(item);
            log.info("📦 Stock replenished for {}: +{} (total available: {})",
                    sku, stock, item.getAvailableStock());
            return item;
        }

        var item = InventoryItem.create(sku, productName, stock);
        inventoryRepository.save(item);
        log.info("📦 New inventory item created: {} with {} units", sku, stock);
        return item;
    }

    /**
     * Adds demo data only when the SKU does not exist. This keeps application
     * restarts from silently increasing stock.
     */
    @Transactional
    public InventoryItem seedInventory(String sku, String productName, int stock) {
        return inventoryRepository.findBySku(sku)
                .orElseGet(() -> {
                    var item = InventoryItem.create(sku, productName, stock);
                    inventoryRepository.save(item);
                    return item;
                });
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> findAll() {
        return inventoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public InventoryItem findBySku(String sku) {
        return inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found: " + sku));
    }

    // ── Event Listeners ─────────────────────────────────────────────
    /**
     * OrderPlacedEvent → reserve stock for each line item. If ALL items can be
     * reserved → publish StockReservedEvent. If ANY item fails → publish
     * StockReservationFailedEvent.
     */
    @KafkaListener(topics = "order-events", groupId = "inventory-context")
    @Transactional
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("📦 Received OrderPlacedEvent for order: {}", event.orderId());

        try {
            // Reserve stock for each line item
            for (var lineItem : event.lineItems()) {
                var inventoryItem = inventoryRepository.findBySku(lineItem.productId())
                        .orElseThrow(() -> new IllegalStateException(
                        "Product not found in inventory: " + lineItem.productId()));

                inventoryItem.reserveStock(lineItem.quantity());
                inventoryRepository.save(inventoryItem);

                var reservation = StockReservation.create(event.orderId(), lineItem.productId(), lineItem.quantity());
                reservationRepository.save(reservation);

                log.info("📦 Reserved {} units of {} for order {}",
                        lineItem.quantity(), lineItem.productId(), event.orderId());
            }

            // All items reserved successfully
            kafkaTemplate.send(INVENTORY_TOPIC, event.orderId(),
                    new StockReservedEvent(
                            event.orderId(),
                            event.totalAmount(),
                            event.currency(),
                            "All items reserved successfully"));

            log.info("✅ Stock reserved for order: {}", event.orderId());

        } catch (Exception e) {
            log.error("❌ Stock reservation failed for order {}: {}", event.orderId(), e.getMessage());

            // Compensate — release any stock that was already reserved for this order
            releaseReservationsForOrder(event.orderId());

            kafkaTemplate.send(INVENTORY_TOPIC, event.orderId(),
                    new StockReservationFailedEvent(event.orderId(), e.getMessage()));
        }
    }

    /**
     * OrderCancelledEvent → release all reserved stock for this order.
     */
    @KafkaListener(topics = "order-events", groupId = "inventory-cancel")
    @Transactional
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("📦 Received OrderCancelledEvent for order: {}", event.orderId());
        releaseReservationsForOrder(event.orderId());
    }

    /**
     * PaymentCapturedEvent is the commit signal for a reservation. Once the
     * payment context succeeds, reserved units become permanently consumed.
     */
    @KafkaListener(topics = "payment-events", groupId = "inventory-payment-capture")
    @Transactional
    public void onPaymentCaptured(PaymentCapturedEvent event) {
        log.info("📦 Received PaymentCapturedEvent for order: {}", event.orderId());

        var reservations = reservationRepository.findByOrderIdAndStatus(
                event.orderId(), StockReservation.ReservationStatus.RESERVED);
        for (var reservation : reservations) {
            var inventoryItem = inventoryRepository.findBySku(reservation.getSku()).orElseThrow();
            inventoryItem.confirmReduction(reservation.getQuantity());
            reservation.confirm();
            inventoryRepository.save(inventoryItem);
            reservationRepository.save(reservation);
        }
    }

    // ── Private helpers ─────────────────────────────────────────────
    private void releaseReservationsForOrder(String orderId) {
        var reservations = reservationRepository
                .findByOrderIdAndStatus(orderId, StockReservation.ReservationStatus.RESERVED);

        for (var reservation : reservations) {
            var inventoryItem = inventoryRepository.findBySku(reservation.getSku()).orElse(null);
            if (inventoryItem != null) {
                inventoryItem.releaseStock(reservation.getQuantity());
                inventoryRepository.save(inventoryItem);
                reservation.release();
                reservationRepository.save(reservation);

                kafkaTemplate.send(INVENTORY_TOPIC, orderId,
                        new StockReleasedEvent(orderId, reservation.getSku(), reservation.getQuantity()));

                log.info("📦 Released {} units of {} for order {}",
                        reservation.getQuantity(), reservation.getSku(), orderId);
            }
        }
    }
}
