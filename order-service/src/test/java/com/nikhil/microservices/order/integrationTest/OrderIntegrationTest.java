package com.nikhil.microservices.order.integrationTest;

import com.nikhil.microservices.order.integrationTest.configs.IntegrationTestConfiguration;
import com.nikhil.microservices.order.integrationTest.stubs.InventoryClientStub;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class OrderIntegrationTest extends IntegrationTestConfiguration {

    private static final String BASE_PATH = "/api/order";

    @Test
    public void shouldCreateOrder(){

        String requestBody = """
                {
                      "skuCode" : "iphone 15",
                      "price" : 100,
                      "quantity" : 10
                  }
                """;

        InventoryClientStub.stubInventoryCall("iphone 15",10);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .body("data.id", Matchers.notNullValue())
                .body("data.id", Matchers.equalTo(1))
                .body("data.skuCode", Matchers.equalTo("iphone 15"))
                .body("data.price", Matchers.equalTo(100))
                .body("data.quantity", Matchers.equalTo(10));

    }

    @Test
    void shouldFailWhenBodyIsEmpty() {

        given()
                .contentType(ContentType.JSON)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(400)
                .body("error.message", Matchers.equalTo("Malformed JSON request"));
    }
}
