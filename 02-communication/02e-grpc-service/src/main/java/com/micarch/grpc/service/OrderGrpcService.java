package com.micarch.grpc.service;

import java.util.UUID;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import com.micarch.grpc.api.GetOrderRequest;
import com.micarch.grpc.api.ListOrdersRequest;
import com.micarch.grpc.api.OrderResponse;
import com.micarch.grpc.api.OrderServiceGrpc;
import com.micarch.grpc.api.PlaceOrderRequest;
import com.micarch.grpc.api.PlaceOrderResponse;
import com.micarch.grpc.domain.OrderStore;

@GrpcService
public class OrderGrpcService extends OrderServiceGrpc.OrderServiceImplBase {

    private final OrderStore orderStore;

    public OrderGrpcService(OrderStore orderStore) {
        this.orderStore = orderStore;
    }

    @Override
    public void placeOrder(PlaceOrderRequest request, StreamObserver<PlaceOrderResponse> observer) {
        if (request.getCustomerId().isBlank() || request.getProductId().isBlank()
                || request.getQuantity() <= 0 || request.getUnitPrice() < 0) {
            observer.onError(Status.INVALID_ARGUMENT
                    .withDescription("customer_id, product_id, quantity, and unit_price are invalid")
                    .asRuntimeException());
            return;
        }

        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        orderStore.save(OrderResponse.newBuilder()
                .setOrderId(orderId)
                .setCustomerId(request.getCustomerId())
                .setProductId(request.getProductId())
                .setQuantity(request.getQuantity())
                .setUnitPrice(request.getUnitPrice())
                .setStatus("ACCEPTED")
                .build());

        observer.onNext(PlaceOrderResponse.newBuilder()
                .setOrderId(orderId)
                .setStatus("ACCEPTED")
                .build());
        observer.onCompleted();
    }

    @Override
    public void getOrder(GetOrderRequest request, StreamObserver<OrderResponse> observer) {
        OrderResponse order = orderStore.find(request.getOrderId());
        if (order == null) {
            observer.onError(Status.NOT_FOUND.withDescription("Order not found: " + request.getOrderId())
                    .asRuntimeException());
            return;
        }
        observer.onNext(order);
        observer.onCompleted();
    }

    @Override
    public void listOrders(ListOrdersRequest request, StreamObserver<OrderResponse> observer) {
        orderStore.findByCustomer(request.getCustomerId()).forEach(observer::onNext);
        observer.onCompleted();
    }
}
