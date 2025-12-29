package com.nikhil.microservices.order.integration;

import com.nikhil.microservices.order.configs.BaseIntegrationTestConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class OrderIntegrationTest extends BaseIntegrationTestConfiguration {

    private static final String BASE_PATH = "/api/order";

    @Test
    public void shouldCreateOrder(){

        String requestBody = """
                {
                     "skuCode" : "iphone 17",
                     "price" : 1000,
                     "quantity" : 101
                 }
                """;

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .body("data.id", Matchers.notNullValue())
                .body("data.id", Matchers.equalTo(1))
                .body("data.skuCode", Matchers.equalTo("iphone 17"))
                .body("data.price", Matchers.equalTo(1000))
                .body("data.quantity", Matchers.equalTo(101));

    }

    @Test
    public void shouldFailWhenPriceIsNull(){

        String requestBody = """
                {
                     "skuCode" : "iphone 17",
                     "price" : null,
                     "quantity" : 101
                 }
                """;

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(400)
                .body("data.id", Matchers.notNullValue())
                .body("data.id", Matchers.equalTo(1))
                .body("data.skuCode", Matchers.equalTo("iphone 17"))
                .body("data.price", Matchers.equalTo(1000))
                .body("data.quantity", Matchers.equalTo(101));

    }
}
