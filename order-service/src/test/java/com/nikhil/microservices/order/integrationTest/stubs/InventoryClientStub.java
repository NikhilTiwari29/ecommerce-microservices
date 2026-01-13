package com.nikhil.microservices.order.integrationTest.stubs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikhil.microservices.order.advices.ApiResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Defines HTTP stubs for the Inventory Service using WireMock.
 *
 * Why WireMock and not Mockito?
 * Mockito mocks Java objects inside the JVM. The Order Service does not call
 * the Inventory Service as a Java method — it calls it over HTTP as a separate
 * microservice. Mockito cannot intercept HTTP traffic, but WireMock can.
 *
 * WireMock runs an embedded HTTP server that simulates the Inventory Service so
 * integration tests can validate real HTTP interactions without calling the
 * actual downstream service.
 */
public class InventoryClientStub {

    /**
     * Stubs the Inventory availability endpoint.
     *
     * When the Order Service calls:
     *   GET /api/inventory?skuCode={skuCode}&quantity={quantity}
     *
     * WireMock will return:
     *   HTTP 200
     *   Body: true
     *
     * This simulates a successful inventory check.
     */

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void stubInventoryCall(String skuCode, Integer quantity) {

        try {
            ApiResponse<Boolean> response =
                    new ApiResponse<>(null, 200, null, true, null);

            String json = mapper.writeValueAsString(response);

            stubFor(get(urlPathEqualTo("/api/inventory"))
                    .withQueryParam("skuCode", equalTo(skuCode))
                    .withQueryParam("quantity", equalTo(quantity.toString()))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(json)
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize ApiResponse for WireMock", e);
        }
    }
}
