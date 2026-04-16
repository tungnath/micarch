package com.micarch.ddd.payment.controller;

import com.micarch.ddd.payment.domain.Payment;
import com.micarch.ddd.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for the Payment bounded context. Read-only API — payments are
 * initiated via events, not REST.
 */
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment Service", description = "Payment Bounded Context API")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    @Operation(summary = "List all payments")
    public List<Payment> listPayments() {
        return paymentService.findAll();
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID")
    public Payment getPayment(@PathVariable("paymentId") String paymentId) {
        return paymentService.findById(paymentId);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payments for an order")
    public List<Payment> getPaymentsByOrder(@PathVariable("orderId") String orderId) {
        return paymentService.findByOrderId(orderId);
    }
}
