package com.phaneendra.messaging.service;

import com.phaneendra.messaging.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderId(String orderId);

    boolean existsByOrderId(String orderId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'COMPLETED'")
    long countCompletedOrders();
}
