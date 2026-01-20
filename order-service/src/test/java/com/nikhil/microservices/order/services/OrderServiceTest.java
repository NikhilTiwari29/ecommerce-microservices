package com.nikhil.microservices.order.services;

import com.nikhil.microservices.order.advices.ApiResponse;
import com.nikhil.microservices.order.client.InventoryClient;
import com.nikhil.microservices.order.dto.OrderRequest;
import com.nikhil.microservices.order.dto.OrderResponse;
import com.nikhil.microservices.order.entities.Order;
import com.nikhil.microservices.order.exceptions.OrderCreationException;
import com.nikhil.microservices.order.repositories.OrderRepository;
import com.nikhil.microservices.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrder_whenInventoryIsAvailable() {

        OrderRequest request = new OrderRequest(1L,"1234","iphone_15", BigDecimal.valueOf(100), 2,null);

        when(inventoryClient.isInStock("iphone_15", 2))
                .thenReturn(new ApiResponse<>(null, 200, "/api/inventory", true, null));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.placeOrder(request);

        assertThat(response.skuCode()).isEqualTo("iphone_15");
        assertThat(response.quantity()).isEqualTo(2);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void shouldThrowException_whenInventoryIsNotAvailable() {

        OrderRequest request = new OrderRequest(1L,"1234","iphone_15", BigDecimal.valueOf(100), 2,null);

        when(inventoryClient.isInStock("iphone_15", 10))
                .thenReturn(new ApiResponse<>(null, 200, "/api/inventory", false, null));

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(OrderCreationException.class)
                .hasMessageContaining("Product with sku code iphone_15 is not in stock");

        verify(orderRepository, never()).save(any());
    }

//    @Test
//    void shouldThrowException_whenInventoryServiceFails() {
//
//         OrderRequest request = new OrderRequest(1L,"1234","iphone_15", BigDecimal.valueOf(100), 2,null);
//
//        Request feignRequest = Request.create(
//                Request.HttpMethod.GET,
//                "/api/inventory",
//                Collections.emptyMap(), // headers
//                null, // body
//                StandardCharsets.UTF_8,
//                new RequestTemplate()
//        );
//
//        when(inventoryClient.isInStock(any(), any()))
//                .thenThrow(new FeignException.InternalServerError(
//                        "Inventory down", feignRequest, null, null
//                ));
//
//        assertThatThrownBy(() -> orderService.placeOrder(request))
//                .isInstanceOf(OrderCreationException.class)
//                .hasMessageContaining("Unable to verify inventory at this time");
//
//        verify(orderRepository, never()).save(any());
//    }

    @Test
    void shouldThrowException_whenDatabaseFails() {

        OrderRequest request = new OrderRequest(1L,"1234","iphone_15", BigDecimal.valueOf(100), 2,null);

        when(inventoryClient.isInStock("iphone_15", 2))
                .thenReturn(new ApiResponse<>(null, 200, "/api/inventory", true, null));

        when(orderRepository.save(any(Order.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("DB error"));

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(OrderCreationException.class)
                .hasMessageContaining("Unable to create order");

        verify(orderRepository).save(any(Order.class));
    }
}
