package com.nikhil.microservices.product.configs;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.mongodb.MongoDBContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTestConfiguration {

    @ServiceConnection
    static final MongoDBContainer mongoDBContainer =
            new MongoDBContainer("mongo:7.0.5");

    @LocalServerPort
    protected Integer port;

    static {
        mongoDBContainer.start();
    }

    @BeforeEach
    void setupRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }
}
