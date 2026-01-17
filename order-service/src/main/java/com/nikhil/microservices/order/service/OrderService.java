package com.nikhil.microservices.order.service;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public OrderResponse placeOrder(OrderRequest orderRequest) {

        log.info("Placing order for SKU={} with quantity={}",
                orderRequest.skuCode(), orderRequest.quantity());

        ApiResponse<Boolean> response;
        try {
            response = inventoryClient.isInStock(
                    orderRequest.skuCode(),
                    orderRequest.quantity()
            );
        } catch (Exception ex) {
            log.error("Inventory call failed hard", ex);
            throw new InventoryUnavailableException("Inventory service unavailable", ex);
        }

        if (response == null || response.data() == null) {
            log.warn("Inventory unavailable for SKU={}", orderRequest.skuCode());
            throw new InventoryUnavailableException("Inventory service unavailable");
        }

        if (!response.data()) {
            log.warn("Insufficient inventory for SKU={}", orderRequest.skuCode());
            throw new InsufficientInventoryException(
                    "Product with sku code " + orderRequest.skuCode() + " is not in stock"
            );
        }

        try {
            Order order = new Order(
                    UUID.randomUUID().toString(),
                    orderRequest.skuCode(),
                    orderRequest.price(),
                    orderRequest.quantity()
            );

            Order savedOrder = orderRepository.save(order);

            log.info("Order created successfully with id={}", savedOrder.getId());

            OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(
                    order.getOrderNumber(),
                    orderRequest.userDetails().email(),
                    orderRequest.userDetails().firstName(),
                    orderRequest.userDetails().lastName()
            );

            log.info("Start - Sending OrderPlacedEvent {}", orderPlacedEvent);

            kafkaTemplate.send("order-placed", order.getOrderNumber(), orderPlacedEvent)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send OrderPlacedEvent {}", orderPlacedEvent, ex);
                        } else {
                            log.info("Successfully sent OrderPlacedEvent {} to partition {} offset {}",
                                    orderPlacedEvent,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });

            return new OrderResponse(
                    savedOrder.getId(),
                    savedOrder.getOrderNumber(),
                    savedOrder.getSkuCode(),
                    savedOrder.getPrice(),
                    savedOrder.getQuantity()
            );


        } catch (DataAccessException ex) {
            log.error("Database error while creating order", ex);
            throw new OrderCreationException("Unable to create order at this time", ex);
        }
    }
}
