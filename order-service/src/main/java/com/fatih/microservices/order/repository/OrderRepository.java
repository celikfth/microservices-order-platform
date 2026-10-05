package com.fatih.microservices.order.repository;

import com.fatih.microservices.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import com.fatih.microservices.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByStatus(
            OrderStatus status,
            Pageable pageable
    );
}