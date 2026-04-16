package com.micarch.ddd.inventory.repository;

import com.micarch.ddd.inventory.domain.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Stock reservation repository — tracks all reservations within the Inventory context.
 */
public interface StockReservationRepository extends JpaRepository<StockReservation, String> {

    List<StockReservation> findByOrderId(String orderId);

    List<StockReservation> findByOrderIdAndStatus(String orderId, StockReservation.ReservationStatus status);
}
