package com.micarch.ddd.domain;

import com.micarch.ddd.payment.domain.Payment;
import com.micarch.ddd.payment.domain.PaymentMethod;
import com.micarch.ddd.payment.domain.PaymentStatus;
import com.micarch.ddd.shared.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentDomainTest {

    @Test
    void paymentCanBeCapturedOnlyOnce() {
        var payment = Payment.initiate(
                "ORDER-1", Money.inr(new BigDecimal("99.99")), PaymentMethod.upi());

        payment.capture();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
        assertThatThrownBy(payment::capture).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void failedPaymentRetainsFailureReason() {
        var payment = Payment.initiate(
                "ORDER-1", Money.inr(new BigDecimal("99.99")), PaymentMethod.upi());

        payment.fail("issuer declined");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getFailureReason()).isEqualTo("issuer declined");
    }
}
