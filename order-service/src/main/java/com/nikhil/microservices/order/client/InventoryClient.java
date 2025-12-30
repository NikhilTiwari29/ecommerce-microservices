package com.nikhil.microservices.order.client;

import com.nikhil.microservices.order.advices.ApiResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

// Feign client is deprecated

//@FeignClient(name = "inventory", url = "${inventory.url}")
//public interface InventoryClient {
//
//    @GetMapping("/api/inventory")
//    ApiResponse<Boolean> isInStock(@RequestParam String skuCode,
//                                   @RequestParam Integer quantity);
//}

// HTTP Interface / Rest Client mechanism is the new approach.
public interface InventoryClient {

    @GetExchange("/api/inventory")
    ApiResponse<Boolean> isInStock(@RequestParam String skuCode,
                                   @RequestParam Integer quantity);
}
