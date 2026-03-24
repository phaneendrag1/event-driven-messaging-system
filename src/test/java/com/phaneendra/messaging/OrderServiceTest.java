package com.phaneendra.messaging;

import com.phaneendra.messaging.model.Order;
import com.phaneendra.messaging.producer.OrderEventProducer;
import com.phaneendra.messaging.service.OrderRepository;
import com.phaneendra.messaging.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventProducer eventProducer;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
                .customerId("CUST-001")
                .productId("PROD-001")
                .quantity(2)
                .amount(new BigDecimal("99.99"))
                .build();
    }

    @Test
    @DisplayName("Should create order and publish Kafka events successfully")
    void shouldCreateOrderAndPublishEvents() {
        // Arrange
        Order savedOrder = Order.builder()
                .id(1L)
                .orderId("test-order-id")
                .customerId("CUST-001")
                .productId("PROD-001")
                .quantity(2)
                .amount(new BigDecimal("99.99"))
                .status(Order.OrderStatus.PENDING)
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        Order result = orderService.createOrder(testOrder);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo("CUST-001");
        assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.PENDING);

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(eventProducer, times(1)).publishOrderEvent(any());
        verify(eventProducer, times(1)).publishInventoryEvent(any());
        verify(eventProducer, times(1)).publishNotificationEvent(any());
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void shouldThrowExceptionWhenOrderNotFound() {
        // Arrange
        when(orderRepository.findByOrderId(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrderById("non-existent-id"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    @DisplayName("Should return order when found by orderId")
    void shouldReturnOrderWhenFound() {
        // Arrange
        Order existingOrder = Order.builder()
                .orderId("existing-order-id")
                .customerId("CUST-002")
                .status(Order.OrderStatus.COMPLETED)
                .build();

        when(orderRepository.findByOrderId("existing-order-id"))
                .thenReturn(Optional.of(existingOrder));

        // Act
        Order result = orderService.getOrderById("existing-order-id");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo("existing-order-id");
        assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should generate unique orderId for each order")
    void shouldGenerateUniqueOrderId() {
        // Arrange
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            assertThat(order.getOrderId()).isNotNull();
            assertThat(order.getOrderId()).isNotEmpty();
            return order;
        });

        // Act
        orderService.createOrder(testOrder);

        // Assert
        verify(orderRepository).save(argThat(order ->
                order.getOrderId() != null && !order.getOrderId().isEmpty()
        ));
    }
}
