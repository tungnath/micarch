package com.micarch.ddd.order.repository;

import com.micarch.ddd.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Order repository — scoped to the Order bounded context ONLY. No cross-context
 * queries allowed through this interface.
 */
public interface OrderRepository extends JpaRepository<Order, String> {
}
