package com.micarch.eventsourcing;

import com.micarch.eventsourcing.events.OrderEvents.OrderPlacedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderEventsTest {
    @Test
    void placedEventContainsTheFactsNeededToRebuildState() {
        var event = new OrderPlacedEvent("O-1", "C-1", "P-1", 2, new BigDecimal("10.00"));
        assertThat(event.orderId()).isEqualTo("O-1");
        assertThat(event.quantity()).isEqualTo(2);
    }
}
