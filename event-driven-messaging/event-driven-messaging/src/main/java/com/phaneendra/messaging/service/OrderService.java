package com.phaneendra.messaging.service;

import com.phaneendra.messaging.model.Order;
import com.phaneendra.messaging.model.OrderEvent;
import com.phaneendra.messaging.producer.OrderEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Order service — handles order creation and publishes events to Kafka.
 * Follows the outbox pattern: save order first, then publish event.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer eventProducer;

    /**
     * Creates a new order and publishes events to Kafka topics.
     * Generates a unique orderId (UUID) to guarantee idempotency
     * across consumer retries.
     */
    @Transactional
    public Order createOrder(Order order) {
        // Generate unique orderId for idempotency
        String orderId = UUID.randomUUID().toString();
        order.setOrderId(orderId);
        order.setStatus(Order.OrderStatus.PENDING);

        // Save order to DB first
        Order savedOrder = orderRepository.save(order);
        log.info("Order created: orderId={}", orderId);

        // Build Kafka event
        OrderEvent event = OrderEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .customerId(order.getCustomerId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .amount(order.getAmount())
                .eventType(OrderEvent.EventType.ORDER_CREATED.name())
                .timestamp(LocalDateTime.now())
                .build();

        // Publish to all downstream topics
        eventProducer.publishOrderEvent(event);
        eventProducer.publishInventoryEvent(event);
        eventProducer.publishNotificationEvent(event);

        return savedOrder;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    public long getCompletedOrderCount() {
        return orderRepository.countCompletedOrders();
    }
}
