package com.micarch.rest.client;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", url = "${services.payment-service-url:http://localhost:8401}")
public interface PaymentClient {

    @PostMapping("/api/payments/capture")
    PaymentResponse capture(@RequestBody PaymentRequest request);

    record PaymentRequest(String orderId, BigDecimal amount, String currency) {
    }

    record PaymentResponse(String paymentId, String status) {
        public boolean captured() {
            return "CAPTURED".equalsIgnoreCase(status);
        }
    }
}
