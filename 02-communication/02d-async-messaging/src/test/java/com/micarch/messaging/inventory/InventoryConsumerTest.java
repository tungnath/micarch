package com.micarch.messaging.inventory;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;

import com.micarch.messaging.order.OrderCreatedEvent;

class InventoryConsumerTest {

    @Test
    void duplicateOrderIsAcknowledgedWithoutSecondReservation() {
        ProcessedOrderStore store = new ProcessedOrderStore();
        InventoryConsumer consumer = new InventoryConsumer(store);
        Acknowledgment ack = mock(Acknowledgment.class);
        OrderCreatedEvent event = new OrderCreatedEvent("ORD-1", "C1", "P1", 2, BigDecimal.TEN);
        ConsumerRecord<String, OrderCreatedEvent> record = new ConsumerRecord<>("order-events", 0, 0, "ORD-1", event);

        consumer.consume(record, ack);
        consumer.consume(record, ack);

        assertTrue(store.contains("ORD-1"));
        verify(ack, org.mockito.Mockito.times(2)).acknowledge();
    }

    @Test
    void retryProductLeavesTheMessageUnacknowledged() {
        InventoryConsumer consumer = new InventoryConsumer(new ProcessedOrderStore());
        Acknowledgment ack = mock(Acknowledgment.class);
        OrderCreatedEvent event = new OrderCreatedEvent("ORD-RETRY", "C1", "RETRY", 1, BigDecimal.ONE);
        ConsumerRecord<String, OrderCreatedEvent> record = new ConsumerRecord<>("order-events", 0, 0, "ORD-RETRY", event);

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> consumer.consume(record, ack));
        org.mockito.Mockito.verifyNoInteractions(ack);
    }
}
