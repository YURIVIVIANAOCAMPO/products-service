package com.store.products.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.UUID;
import java.util.Map;

@Component
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(@Value("${inventory.service.url:http://localhost:8081}") @NonNull String baseUrl,
                           @Value("${security.api-key:inventory-secret-key}") String apiKey) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(2000);

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(baseUrl)
                .defaultHeader("x-api-key", apiKey)
                .build();
    }

    @Retry(name = "inventoryService")
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "getAvailableStockFallback")
    public int getAvailableStock(@NonNull UUID productId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.get()
                    .uri("/inventory/{productId}", productId)
                    .retrieve()
                    .body(Map.class);
            
            if (response != null && response.get("available") != null) {
                return (Integer) response.get("available");
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }

    public int getAvailableStockFallback(UUID productId, Throwable t) {
        return 0;
    }

    @Retry(name = "inventoryService")
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "deductStockFallback")
    public boolean deductStock(UUID productId, int quantity, String idempotencyKey) {
        restClient.post()
                .uri("/inventory/purchases")
                .header("Idempotency-Key", idempotencyKey)
                .body(java.util.Objects.requireNonNull(Map.of("productId", productId, "quantity", quantity)))
                .retrieve()
                .toBodilessEntity();
        return true;
    }

    @Retry(name = "inventoryService")
    public void initializeStock(UUID productId, int quantity) {
        restClient.put()
                .uri("/inventory/{productId}/stock?quantity={quantity}", productId, quantity)
                .retrieve()
                .toBodilessEntity();
    }

    public boolean deductStockFallback(UUID productId, int quantity, String idempotencyKey, Throwable t) {
        return false;
    }
}
