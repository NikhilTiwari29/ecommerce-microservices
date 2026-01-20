package com.nikhil.microservices.order.services;

import com.nikhil.microservices.order.advices.ApiResponse;
import com.nikhil.microservices.order.client.InventoryClient;
import com.nikhil.microservices.order.dto.OrderRequest;
import com.nikhil.microservices.order.dto.OrderResponse;
import com.nikhil.microservices.order.entities.Order;
import com.nikhil.microservices.order.events.OrderPlacedEvent;
import com.nikhil.microservices.order.exceptions.InsufficientInventoryException;
import com.nikhil.microservices.order.exceptions.InventoryUnavailableException;
import com.nikhil.microservices.order.exceptions.OrderCreationException;
import com.nikhil.microservices.order.repositories.OrderRepository;
import com.nikhil.microservices.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    private OrderRequest.UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new OrderRequest.UserDetails(
                "nik@gmail.com",
                "nikhil",
                "tiwari"
        );
    }

    @Test
    void shouldCreateOrder_whenInventoryIsAvailable() {

        OrderRequest request = new OrderRequest(
                1L,
                "1234",
                "iphone_15",
                BigDecimal.valueOf(100),
                2,
                userDetails
        );

        when(inventoryClient.isInStock("iphone_15", 2))
                .thenReturn(new ApiResponse<>(null, 200, "/api/inventory", true, null));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(kafkaTemplate.send(eq("order-placed"), anyString(), any(OrderPlacedEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        OrderResponse response = orderService.placeOrder(request);

        assertThat(response.skuCode()).isEqualTo("iphone_15");
        assertThat(response.quantity()).isEqualTo(2);

        verify(orderRepository).save(any(Order.class));
        verify(kafkaTemplate).send(eq("order-placed"), anyString(), any(OrderPlacedEvent.class));
    }

    @Test
    void shouldThrowException_whenInventoryIsInsufficient() {

        OrderRequest request = new OrderRequest(
                1L,
                "1234",
                "iphone_15",
                BigDecimal.valueOf(100),
                2,
                userDetails
        );

        when(inventoryClient.isInStock("iphone_15", 2))
                .thenReturn(new ApiResponse<>(null, 200, "/api/inventory", false, null));

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(InsufficientInventoryException.class)
                .hasMessageContaining("iphone_15");

        verify(orderRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void shouldThrowException_whenInventoryServiceReturnsNullResponse() {

        OrderRequest request = new OrderRequest(
                1L,
                "1234",
                "iphone_15",
                BigDecimal.valueOf(100),
                2,
                userDetails
        );

        when(inventoryClient.isInStock("iphone_15", 2))
                .thenReturn(null);

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(InventoryUnavailableException.class)
                .hasMessageContaining("Inventory service unavailable");

        verify(orderRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void shouldThrowException_whenInventoryServiceThrowsException() {

        OrderRequest request = new OrderRequest(
                1L,
                "1234",
                "iphone_15",
                BigDecimal.valueOf(100),
                2,
                userDetails
        );

        when(inventoryClient.isInStock(anyString(), anyInt()))
                .thenThrow(new RuntimeException("Timeout"));

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(InventoryUnavailableException.class)
                .hasMessageContaining("Inventory service unavailable");

        verify(orderRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void shouldThrowException_whenDatabaseFails() {

        OrderRequest request = new OrderRequest(
                1L,
                "1234",
                "iphone_15",
                BigDecimal.valueOf(100),
                2,
                userDetails
        );

        when(inventoryClient.isInStock("iphone_15", 2))
                .thenReturn(new ApiResponse<>(null, 200, "/api/inventory", true, null));

        when(orderRepository.save(any(Order.class)))
                .thenThrow(new DataAccessException("DB error") {});

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(OrderCreationException.class)
                .hasMessageContaining("Unable to create order");

        verify(orderRepository).save(any(Order.class));
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }
}
