package com.nikhil.microservices.inventory.integration;

import com.nikhil.microservices.inventory.configs.BaseIntegrationTestConfiguration;
import com.nikhil.microservices.inventory.entities.Inventory;
import com.nikhil.microservices.inventory.repositories.InventoryRepository;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class InventoryControllerIT extends BaseIntegrationTestConfiguration {

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void setUp() {
        inventoryRepository.deleteAll();

        Inventory inventory = new Inventory("iphone_15",10);
        inventoryRepository.save(inventory);
    }

    @Test
    void shouldReturnTrue_whenStockIsSufficient() {
        RestAssured
                .given()
                .queryParam("skuCode", "iphone_15")
                .queryParam("quantity", 5)
                .when()
                .get("/api/inventory")
                .then()
                .statusCode(200)
                .body("data", Matchers.equalTo(true));
    }

    @Test
    void shouldReturnFalse_whenStockIsInsufficient() {
        RestAssured
                .given()
                .queryParam("skuCode", "iphone_15")
                .queryParam("quantity", 20)
                .when()
                .get("/api/inventory")
                .then()
                .statusCode(200)
                .body("data", Matchers.equalTo(false));
    }

    @Test
    void shouldReturnFalse_whenSkuDoesNotExist() {
        RestAssured
                .given()
                .queryParam("skuCode", "unknown")
                .queryParam("quantity", 1)
                .when()
                .get("/api/inventory")
                .then()
                .statusCode(200)
                .body("data", Matchers.equalTo(false));
    }
}
