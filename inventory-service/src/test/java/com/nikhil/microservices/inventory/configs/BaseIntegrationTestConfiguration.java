package com.nikhil.microservices.inventory.configs;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTestConfiguration {

    @ServiceConnection
    static final MySQLContainer<?> mongoDBContainer =
            new MySQLContainer<>("mysql:8.0.36");

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
