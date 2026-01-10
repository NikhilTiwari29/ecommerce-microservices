package com.nikhil.microservices.product.integrationTest;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for all integration tests.
 *
 * Responsibilities:
 *  - Bootstraps the full Spring Boot application context on a random port
 *  - Starts and manages a MongoDB Testcontainer
 *  - Injects the container's connection details into Spring Boot automatically
 *  - Configures RestAssured once for all extending test classes
 *
 * All integration test classes should extend this class.
 */
@Testcontainers
// Enables Testcontainers JUnit Jupiter integration so that containers annotated with @Container
// are started before tests and stopped after tests.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// Starts the full Spring Boot context on a random available port for isolation between test runs.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// Uses a single test class instance so that @BeforeAll can be a non-static method.
public abstract class BaseIntegrationTest {

    @Container
    // Marks this field as a Testcontainers-managed container.
    // Testcontainers will automatically start and stop this container around the test lifecycle.

    @ServiceConnection
    // Tells Spring Boot (3.1+) that this container provides a service (MongoDB).
    // Spring Boot will automatically extract the connection details (URI) and
    // wire them into the application context (spring.data.mongodb.uri).
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.5");

    @LocalServerPort
    // Injects the random port that Spring Boot started the embedded server on.
    // This allows RestAssured to send HTTP requests to the correct port.
    private int port;

    @BeforeAll
        // Executed once before any test method in subclasses.
        // Configures RestAssured globally so that individual tests do not need to repeat this setup.
    void setupRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }
}
