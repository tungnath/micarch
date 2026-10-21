package com.micarch.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import com.micarch.rest.client.InventoryClient;
import com.micarch.rest.client.PaymentClient;
import com.micarch.rest.domain.SyncOrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SyncOrderServiceTest {

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private PaymentClient paymentClient;

    @Test
    void confirmsOrderAfterSynchronousInventoryAndPaymentCalls() {
        when(inventoryClient.checkStock("P-1")).thenReturn(new InventoryClient.StockResponse("P-1", 10));
        when(paymentClient.capture(any())).thenReturn(new PaymentClient.PaymentResponse("PAY-1", "CAPTURED"));

        var result = new SyncOrderService(inventoryClient, paymentClient)
                .placeOrder("P-1", 2, new BigDecimal("49.99"));

        assertEquals("CONFIRMED", result.status());
        assertEquals(new BigDecimal("99.98"), result.totalAmount());
        verify(inventoryClient).reserveStock("P-1", 2);
        verify(paymentClient).capture(any());
    }

    @Test
    void rejectsBeforePaymentWhenStockIsInsufficient() {
        when(inventoryClient.checkStock("OUT")).thenReturn(new InventoryClient.StockResponse("OUT", 0));

        var result = new SyncOrderService(inventoryClient, paymentClient)
                .placeOrder("OUT", 1, new BigDecimal("10.00"));

        assertEquals("REJECTED", result.status());
        verify(paymentClient, org.mockito.Mockito.never()).capture(any());
    }
}
