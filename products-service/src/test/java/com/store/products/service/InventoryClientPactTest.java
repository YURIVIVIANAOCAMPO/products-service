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

    @Pact(consumer = "products-service")
    public V4Pact createPactForStock(PactBuilder builder) {
        return builder.usingLegacyDsl()
                .given("Product PROD-001 exists with stock 15")
                .uponReceiving("A request to get available stock")
                .path("/api/inventory/PROD-001/stock")
                .method("GET")
                .willRespondWith()
                .status(200)
                .matchHeader("Content-Type", "application/json")
                .body("15")
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "createPactForStock")
    void testGetAvailableStock(MockServer mockServer) {
        InventoryClient inventoryClient = new InventoryClient(mockServer.getUrl(), "test-api-key");
        int stock = inventoryClient.getAvailableStock("PROD-001");
        assertEquals(15, stock);
    }
}
