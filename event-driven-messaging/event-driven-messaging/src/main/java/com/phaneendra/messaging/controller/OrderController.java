package com.phaneendra.messaging.controller;

import com.phaneendra.messaging.model.Order;
import com.phaneendra.messaging.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller exposing order management endpoints.
 * All endpoints follow RESTful conventions.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/v1/orders
     * Creates a new order and publishes events to Kafka.
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody Order order) {
        log.info("Received order creation request for customerId={}", order.getCustomerId());
        Order created = orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/v1/orders
     * Returns all orders.
     */
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /**
     * GET /api/v1/orders/{orderId}
     * Returns a specific order by orderId.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    /**
     * GET /api/v1/orders/stats
     * Returns basic order statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of(
                "completedOrders", orderService.getCompletedOrderCount(),
                "status", "running"
        ));
    }

    /**
     * GET /api/v1/orders/health
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
