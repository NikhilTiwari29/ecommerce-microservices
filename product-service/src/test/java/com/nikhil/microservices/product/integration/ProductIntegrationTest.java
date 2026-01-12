package com.nikhil.microservices.product.integration;

import com.nikhil.microservices.product.integration.config.IntegrationTestConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class ProductIntegrationTest extends IntegrationTestConfiguration {

    private static final String BASE_PATH = "/api/product";

    @Test
    void shouldCreateProduct() {

        String requestBody = """
                {
                    "name": "iphone 17",
                    "description": "iphone 17 is smartphone from apple",
                    "price": 1000
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .body("data.id", Matchers.notNullValue())
                .body("data.name", Matchers.equalTo("iphone 17"))
                .body("data.description", Matchers.equalTo("iphone 17 is smartphone from apple"))
                .body("data.price", Matchers.equalTo(1000));
    }

    @Test
    void shouldFailWhenPriceIsZero() {

        String requestBody = """
                {
                    "name": "iphone",
                    "description": "desc",
                    "price": 0
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(400)
                .body("status", Matchers.equalTo(400))
                .body("path", Matchers.equalTo(BASE_PATH))
                .body("error.message", Matchers.equalTo("Validation failed"))
                .body("error.fieldErrors[0].field", Matchers.equalTo("price"))
                .body("error.fieldErrors[0].message",
                        Matchers.containsString("Product price must be greater than 0"));
    }

    @Test
    void shouldFailWhenNameIsMissing() {

        String requestBody = """
                {
                    "description": "desc",
                    "price": 1000
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(400)
                .body("error.fieldErrors[0].field", Matchers.equalTo("name"))
                .body("error.fieldErrors[0].message",
                        Matchers.containsString("Product name must not be empty"));
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

    @Test
    void shouldReturnEmptyListWhenNoProducts() {

        given()
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("data", Matchers.hasSize(0));
    }

    @Test
    void shouldReturnMultipleProducts() {

        createProduct("iphone", "apple phone", 1000);
        createProduct("pixel", "google phone", 900);

        given()
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("data", Matchers.hasSize(3))
                .body("data.name", Matchers.hasItems("iphone", "pixel", "iphone 17"));
    }

    private void createProduct(String name, String description, int price) {

        String requestBody = String.format("""
                {
                    "name": "%s",
                    "description": "%s",
                    "price": %d
                }
                """, name, description, price);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(201);
    }
}