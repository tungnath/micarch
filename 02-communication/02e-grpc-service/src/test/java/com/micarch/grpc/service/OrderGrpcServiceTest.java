package com.micarch.grpc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.micarch.grpc.api.GetOrderRequest;
import com.micarch.grpc.api.ListOrdersRequest;
import com.micarch.grpc.api.OrderResponse;
import com.micarch.grpc.api.PlaceOrderRequest;
import com.micarch.grpc.api.PlaceOrderResponse;
import com.micarch.grpc.domain.OrderStore;

import io.grpc.stub.StreamObserver;

class OrderGrpcServiceTest {

    @Test
    void placesAndReadsAnOrder() {
        OrderGrpcService service = new OrderGrpcService(new OrderStore());
        CapturingObserver<PlaceOrderResponse> placed = new CapturingObserver<>();

        service.placeOrder(PlaceOrderRequest.newBuilder()
                .setCustomerId("C1").setProductId("P1").setQuantity(2).setUnitPrice(10.5).build(), placed);

        assertEquals("ACCEPTED", placed.values.get(0).getStatus());
        CapturingObserver<OrderResponse> fetched = new CapturingObserver<>();
        service.getOrder(GetOrderRequest.newBuilder().setOrderId(placed.values.get(0).getOrderId()).build(), fetched);
        assertEquals("P1", fetched.values.get(0).getProductId());
    }

    @Test
    void streamsOrdersForACustomer() {
        OrderGrpcService service = new OrderGrpcService(new OrderStore());
        CapturingObserver<PlaceOrderResponse> placed = new CapturingObserver<>();
        PlaceOrderRequest request = PlaceOrderRequest.newBuilder()
                .setCustomerId("C1").setProductId("P1").setQuantity(1).setUnitPrice(1).build();
        service.placeOrder(request, placed);

        CapturingObserver<OrderResponse> streamed = new CapturingObserver<>();
        service.listOrders(ListOrdersRequest.newBuilder().setCustomerId("C1").build(), streamed);
        assertTrue(streamed.completed);
        assertEquals(1, streamed.values.size());
    }

    private static final class CapturingObserver<T> implements StreamObserver<T> {
        private final List<T> values = new ArrayList<>();
        private boolean completed;

        @Override public void onNext(T value) { values.add(value); }
        @Override public void onError(Throwable error) { throw new AssertionError(error); }
        @Override public void onCompleted() { completed = true; }
    }
}
