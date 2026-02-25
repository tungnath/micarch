package com.micarch.cqrs.query.repository;

import com.micarch.cqrs.query.model.OrderView;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OrderViewRepository extends MongoRepository<OrderView, String> {

    Optional<OrderView> findByOrderId(Long orderId);

    List<OrderView> findByCustomerId(String customerId);

    List<OrderView> findByState(String state);
}
