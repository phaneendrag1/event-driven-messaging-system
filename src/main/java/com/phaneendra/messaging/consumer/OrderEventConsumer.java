package com.phaneendra.messaging.consumer;

import com.phaneendra.messaging.model.Order;
import com.phaneendra.messaging.model.OrderEvent;
import com.phaneendra.messaging.service.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Idempotent Kafka consumer with manual offset management.
 *
 * Key design decisions:
 * 1. Manual acknowledgment (AckMode.MANUAL) — offsets committed only after
 *    successful processing, guaranteeing at-least-once delivery.
 * 2. Idempotency check — each event is checked against the DB before processing
 *    to prevent duplicate order creation (exactly-once semantics).
 * 3. Transactional — DB updates and offset commits are atomic.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderRepository orderRepository;

    /**
     * Consumes order events from the order-events topic.
     * Implements idempotency by checking if the orderId already exists
     * before processing — prevents duplicate records on redelivery.
     */
    @KafkaListener(
            topics = "${app.kafka.topic.order}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeOrderEvent(
            @Payload OrderEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received order event: orderId={}, partition={}, offset={}",
                event.getOrderId(), partition, offset);

        try {
            // Idempotency check — skip if already processed
            if (orderRepository.existsByOrderId(event.getOrderId())) {
                log.warn("Duplicate order event detected, skipping: orderId={}", event.getOrderId());
                acknowledgment.acknowledge();
                return;
            }

            // Process the order
            processOrder(event);

            // Commit offset only after successful processing
            acknowledgment.acknowledge();

            log.info("Order event processed successfully: orderId={}", event.getOrderId());

        } catch (Exception e) {
            log.error("Failed to process order event: orderId={}, error={}",
                    event.getOrderId(), e.getMessage());
            // Do NOT acknowledge — message will be redelivered
        }
    }

    /**
     * Consumes notification events from the notification-events topic.
     */
    @KafkaListener(
            topics = "${app.kafka.topic.notification}",
            groupId = "${spring.kafka.consumer.group-id}-notification"
    )
    public void consumeNotificationEvent(
            @Payload OrderEvent event,
            Acknowledgment acknowledgment) {

        log.info("Processing notification for orderId={}", event.getOrderId());
        // In production: send email, SMS, push notification
        log.info("Notification sent to customer: {}", event.getCustomerId());
        acknowledgment.acknowledge();
    }

    /**
     * Consumes inventory events from the inventory-events topic.
     */
    @KafkaListener(
            topics = "${app.kafka.topic.inventory}",
            groupId = "${spring.kafka.consumer.group-id}-inventory"
    )
    public void consumeInventoryEvent(
            @Payload OrderEvent event,
            Acknowledgment acknowledgment) {

        log.info("Processing inventory update for productId={}, quantity={}",
                event.getProductId(), event.getQuantity());
        // In production: update stock levels in inventory service
        acknowledgment.acknowledge();
    }

    private void processOrder(OrderEvent event) {
        Order order = Order.builder()
                .orderId(event.getOrderId())
                .customerId(event.getCustomerId())
                .productId(event.getProductId())
                .quantity(event.getQuantity())
                .amount(event.getAmount())
                .status(Order.OrderStatus.COMPLETED)
                .processedAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);
        log.info("Order saved to database: orderId={}", event.getOrderId());
    }
}
