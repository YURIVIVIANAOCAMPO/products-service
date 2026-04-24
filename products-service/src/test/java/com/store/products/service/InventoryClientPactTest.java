package com.store.products.service;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "inventory-service")
@SuppressWarnings("null")
class InventoryClientPactTest {

    private static final String PRODUCT_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Pact(consumer = "products-service")
    public V4Pact createPactForStock(PactBuilder builder) {
        return builder.usingLegacyDsl()
                .given("Product exists with stock 15")
                .uponReceiving("A request to get available stock")
                .path("/inventory/" + PRODUCT_ID)
                .method("GET")
                .willRespondWith()
                .status(200)
                .matchHeader("Content-Type", "application/json")
                .body("{\"available\": 15}")
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "createPactForStock")
    void testGetAvailableStock(MockServer mockServer) {
        InventoryClient inventoryClient = new InventoryClient(mockServer.getUrl(), "test-api-key");
        int stock = inventoryClient.getAvailableStock(java.util.UUID.fromString(PRODUCT_ID));
        assertEquals(15, stock);
    }
}
