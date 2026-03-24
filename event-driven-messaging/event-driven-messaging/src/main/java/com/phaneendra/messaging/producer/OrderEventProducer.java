package com.phaneendra.messaging.producer;

import com.phaneendra.messaging.model.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer for publishing order events.
 * Uses orderId as the message key to guarantee ordering
 * within the same partition for the same order.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Value("${app.kafka.topic.order}")
    private String orderTopic;

    @Value("${app.kafka.topic.notification}")
    private String notificationTopic;

    @Value("${app.kafka.topic.inventory}")
    private String inventoryTopic;

    /**
     * Publishes an order event to the order-events topic.
     * Uses orderId as key to ensure messages for the same
     * order always go to the same partition (ordering guarantee).
     */
    public void publishOrderEvent(OrderEvent event) {
        log.info("Publishing order event: orderId={}, type={}", event.getOrderId(), event.getEventType());

        CompletableFuture<SendResult<String, OrderEvent>> future =
                kafkaTemplate.send(orderTopic, event.getOrderId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Order event published successfully: orderId={}, partition={}, offset={}",
                        event.getOrderId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish order event: orderId={}, error={}",
                        event.getOrderId(), ex.getMessage());
            }
        });
    }

    /**
     * Publishes a notification event to the notification-events topic.
     */
    public void publishNotificationEvent(OrderEvent event) {
        log.info("Publishing notification event for orderId={}", event.getOrderId());
        kafkaTemplate.send(notificationTopic, event.getOrderId(), event);
    }

    /**
     * Publishes an inventory update event to the inventory-events topic.
     */
    public void publishInventoryEvent(OrderEvent event) {
        log.info("Publishing inventory event for orderId={}", event.getOrderId());
        kafkaTemplate.send(inventoryTopic, event.getOrderId(), event);
    }
}
