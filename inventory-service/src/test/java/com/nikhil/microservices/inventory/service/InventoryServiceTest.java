package com.nikhil.microservices.inventory.service;

import com.nikhil.microservices.inventory.repositories.InventoryRepository;
import com.nikhil.microservices.inventory.services.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void shouldReturnTrue_whenStockIsSufficient() {
        when(inventoryRepository.existsBySkuCodeAndQuantityGreaterThanEqual("iphone_15", 5))
                .thenReturn(true);

        boolean result = inventoryService.isInStock("iphone_15", 5);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalse_whenStockIsInsufficient() {
        when(inventoryRepository.existsBySkuCodeAndQuantityGreaterThanEqual("iphone_15", 20))
                .thenReturn(false);

        boolean result = inventoryService.isInStock("iphone_15", 20);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalse_whenSkuDoesNotExist() {
        when(inventoryRepository.existsBySkuCodeAndQuantityGreaterThanEqual("unknown", 1))
                .thenReturn(false);

        boolean result = inventoryService.isInStock("unknown", 1);

        assertThat(result).isFalse();
    }
}
