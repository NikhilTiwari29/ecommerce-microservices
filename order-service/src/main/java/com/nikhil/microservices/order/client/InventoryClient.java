package com.nikhil.microservices.order.client;

import com.nikhil.microservices.order.advices.ApiErrorResponse;
import com.nikhil.microservices.order.advices.ApiResponse;
import groovy.util.logging.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import java.time.OffsetDateTime;

// Feign client is deprecated

@FeignClient(name = "inventory", url = "${inventory.service.url}")
public interface InventoryClient {

    @GetMapping("/api/inventory")
    ApiResponse<Boolean> isInStock(@RequestParam String skuCode,
                                   @RequestParam Integer quantity);
}

// HTTP Interface / Rest Client mechanism is the new approach.

//@Slf4j
//public interface InventoryClient {
//
//    Logger log = LoggerFactory.getLogger(InventoryClient.class);
//
//    @GetExchange("/api/inventory")
//    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
//    @Retry(name = "inventory")
//    ApiResponse<Boolean> isInStock(@RequestParam String skuCode,
//                                   @RequestParam Integer quantity);
//
//    default ApiResponse<Boolean> fallbackMethod(String skuCode, Integer quantity, Throwable throwable) {
//        log.warn("Inventory fallback triggered for skuCode={}, quantity={}, reason={}",
//                skuCode, quantity, throwable.getMessage());
//
//        return new ApiResponse<>(
//                OffsetDateTime.now().toString(),
//                503,
//                "/api/inventory",
//                null, // important: not false
//                new ApiErrorResponse("Inventory service unavailable", null)
//        );
//    }
//
//}
