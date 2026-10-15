package com.micarch.ddd.domain;

import com.micarch.ddd.order.domain.Order;
import com.micarch.ddd.order.domain.OrderLineItem;
import com.micarch.ddd.order.domain.OrderStatus;
import com.micarch.ddd.order.domain.ShippingAddress;
import com.micarch.ddd.shared.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderDomainTest {

    private static final ShippingAddress ADDRESS = new ShippingAddress(
            "1 MG Road", "Bengaluru", "Karnataka", "560001", "IN");

    @Test
    void createCalculatesTotalAndStartsPending() {
        var order = Order.create("CUST-1", List.of(
                new OrderLineItem("SKU-1", "Keyboard", 2, Money.inr(new BigDecimal("1500"))),
                new OrderLineItem("SKU-2", "Mouse", 1, Money.inr(new BigDecimal("500")))), ADDRESS);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getTotalAmount().getAmount()).isEqualByComparingTo("3500");
    }

    @Test
    void lifecycleRejectsInvalidTransitions() {
        var order = Order.create("CUST-1", List.of(
                new OrderLineItem("SKU-1", "Keyboard", 1, Money.inr(new BigDecimal("1500")))), ADDRESS);

        assertThatThrownBy(order::ship).isInstanceOf(IllegalStateException.class);
        order.confirm();
        order.ship();
        order.deliver();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThatThrownBy(order::cancel).isInstanceOf(IllegalStateException.class);
    }
}
