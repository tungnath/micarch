package com.micarch.ddd.payment.repository;

import com.micarch.ddd.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Payment repository — scoped to the Payment bounded context ONLY.
 */
public interface PaymentRepository extends JpaRepository<Payment, String> {

    List<Payment> findByOrderId(String orderId);
}
