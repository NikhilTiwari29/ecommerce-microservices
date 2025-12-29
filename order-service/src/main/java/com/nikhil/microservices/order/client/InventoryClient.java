package com.nikhil.microservices.order.client;

import com.nikhil.microservices.order.advices.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory", url = "http://localhost:8082")
public interface InventoryClient {

    @GetMapping("/api/inventory")
    ApiResponse<Boolean> isInStock(@RequestParam String skuCode,
                                   @RequestParam Integer quantity);
}
