package com.store.inventory.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.UUID;

@Component
public class ProductsClient {

    private final RestClient restClient;

    public ProductsClient(@Value("${products.service.url:http://localhost:8080}") @NonNull String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public void syncStatus(UUID productId, int stock) {
        try {
            restClient.patch()
                    .uri("/products/{id}/sync-status?stock={stock}", productId, stock)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            // Silently fail or log. In a real system, use an event bus (RabbitMQ/Kafka)
            // for eventual consistency.
        }
    }
}
