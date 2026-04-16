package com.micarch.ddd.payment.service;

import com.micarch.ddd.inventory.event.StockReservedEvent;
import com.micarch.ddd.payment.domain.Payment;
import com.micarch.ddd.payment.domain.PaymentMethod;
import com.micarch.ddd.payment.domain.PaymentStatus;
import com.micarch.ddd.payment.event.PaymentCapturedEvent;
import com.micarch.ddd.payment.event.PaymentFailedEvent;
import com.micarch.ddd.payment.repository.PaymentRepository;
import com.micarch.ddd.shared.vo.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

/**
 * Application Service for the Payment bounded context.
 * Reacts to Inventory events (StockReserved) to process payments.
 *
 * IMPORTANT: This simulates payment processing. In a real system, this would
 * integrate with a payment gateway (Razorpay, Stripe, etc.) via an
 * Anti-Corruption Layer.
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final String PAYMENT_TOPIC = "payment-events";

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Random random = new Random();

    public PaymentService(PaymentRepository paymentRepository,
                          KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // ── Queries ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Payment> findByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public Payment findById(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
    }

    // ── Event Listeners ─────────────────────────────────────────────

    /**
     * StockReservedEvent → initiate and process payment.
     * Simulates payment capture with 90% success rate for demo purposes.
     */
    @KafkaListener(topics = "inventory-events", groupId = "payment-context",
            properties = "spring.json.value.default.type=com.micarch.ddd.inventory.event.StockReservedEvent")
    @Transactional
    public void onStockReserved(StockReservedEvent event) {
        log.info("💳 Received StockReservedEvent for order: {}", event.orderId());

        // In a real system, the amount would come from the event or be looked up.
        // Here we simulate by creating a payment with a placeholder amount.
        var payment = Payment.initiate(
                event.orderId(),
                Money.inr(BigDecimal.ZERO),  // Amount will be set from order context in real impl
                PaymentMethod.upi()           // Default to UPI for demo
        );

        // Simulate payment gateway processing (90% success)
        boolean paymentSuccessful = random.nextInt(100) < 90;

        if (paymentSuccessful) {
            payment.capture();
            paymentRepository.save(payment);

            kafkaTemplate.send(PAYMENT_TOPIC, event.orderId(),
                    new PaymentCapturedEvent(
                            payment.getPaymentId(),
                            event.orderId(),
                            payment.getAmount().getAmount(),
                            payment.getAmount().getCurrency()));

            log.info("✅ Payment captured: {} for order {}", payment.getPaymentId(), event.orderId());
        } else {
            payment.fail("Payment gateway timeout (simulated)");
            paymentRepository.save(payment);

            kafkaTemplate.send(PAYMENT_TOPIC, event.orderId(),
                    new PaymentFailedEvent(
                            payment.getPaymentId(),
                            event.orderId(),
                            "Payment gateway timeout (simulated)"));

            log.info("❌ Payment failed: {} for order {}", payment.getPaymentId(), event.orderId());
        }
    }
}
