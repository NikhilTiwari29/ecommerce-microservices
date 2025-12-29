package com.nikhil.microservices.order.service;

import com.nikhil.microservices.order.dto.OrderRequest;
import com.nikhil.microservices.order.dto.OrderResponse;
import com.nikhil.microservices.order.entities.Order;
import com.nikhil.microservices.order.exceptions.OrderCreationException;
import com.nikhil.microservices.order.repositories.OrderRepository;
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

    public OrderResponse placeOrder(OrderRequest orderRequest){
        try {

            Order order = new Order(
                    UUID.randomUUID().toString(),
                    orderRequest.skuCode(),
                    orderRequest.price(),
                    orderRequest.quantity()
            );

            orderRepository.save(order);

            return new OrderResponse(
                    order.getId(),
                    order.getOrderNumber(),
                    order.getSkuCode(),
                    order.getPrice(),
                    order.getQuantity()
            );

        }catch (DataAccessException ex) {
            log.error("Database error while creating order", ex);
            throw new OrderCreationException("Unable to create order at this time", ex);
        }
    }
}
