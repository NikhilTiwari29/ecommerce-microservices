package com.nikhil.microservices.order.service;

import com.nikhil.microservices.order.advices.ApiResponse;
import com.nikhil.microservices.order.client.InventoryClient;
import com.nikhil.microservices.order.dto.OrderRequest;
import com.nikhil.microservices.order.dto.OrderResponse;
import com.nikhil.microservices.order.entities.Order;
import com.nikhil.microservices.order.exceptions.OrderCreationException;
import com.nikhil.microservices.order.repositories.OrderRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    public OrderResponse placeOrder(OrderRequest orderRequest) {

        try {
            log.info("Placing order for SKU={} with quantity={}",
                    orderRequest.skuCode(), orderRequest.quantity());

            ApiResponse<Boolean> response = inventoryClient.isInStock(
                    orderRequest.skuCode(),
                    orderRequest.quantity()
            );

            Boolean inStock = response.data();

            if (!inStock) {
                log.warn("Insufficient inventory for SKU={}", orderRequest.skuCode());
                throw new OrderCreationException(
                        "Product with sku code " + orderRequest.skuCode() + " is not in stock"
                );
            }

            Order order = new Order(
                    UUID.randomUUID().toString(),
                    orderRequest.skuCode(),
                    orderRequest.price(),
                    orderRequest.quantity()
            );

            Order savedOrder = orderRepository.save(order);

            log.info("Order created successfully with id={}", savedOrder.getId());

            return new OrderResponse(
                    savedOrder.getId(),
                    savedOrder.getOrderNumber(),
                    savedOrder.getSkuCode(),
                    savedOrder.getPrice(),
                    savedOrder.getQuantity()
            );

        } catch (FeignException ex) {
            log.error("Inventory service call failed", ex);
            throw new OrderCreationException("Unable to verify inventory at this time", ex);

        } catch (DataAccessException ex) {
            log.error("Database error while creating order", ex);
            throw new OrderCreationException("Unable to create order at this time", ex);
        }
    }
}
