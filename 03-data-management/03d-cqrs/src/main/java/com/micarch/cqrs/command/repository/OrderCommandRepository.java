package com.micarch.cqrs.command.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.micarch.cqrs.command.domain.OrderAggregate;

public interface OrderCommandRepository extends JpaRepository<OrderAggregate, Long> {
}
