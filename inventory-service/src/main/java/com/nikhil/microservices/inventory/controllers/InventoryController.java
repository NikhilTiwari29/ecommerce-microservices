package com.nikhil.microservices.inventory.controllers;

import com.nikhil.microservices.inventory.services.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<Boolean> isInStock(@RequestParam String skuCode,
                                             @RequestParam Integer quantity) {

        boolean inStock = inventoryService.isInStock(skuCode, quantity);
        return ResponseEntity.ok(inStock);
    }
}
