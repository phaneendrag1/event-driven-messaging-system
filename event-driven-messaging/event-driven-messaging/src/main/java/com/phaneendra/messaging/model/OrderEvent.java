package com.phaneendra.messaging.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event DTO published to Kafka topics.
 * Used for async communication between microservices.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private String eventId;
    private String orderId;
    private String customerId;
    private String productId;
    private Integer quantity;
    private BigDecimal amount;
    private String eventType;
    private LocalDateTime timestamp;

    public enum EventType {
        ORDER_CREATED,
        ORDER_PROCESSING,
        ORDER_COMPLETED,
        ORDER_FAILED
    }
}
