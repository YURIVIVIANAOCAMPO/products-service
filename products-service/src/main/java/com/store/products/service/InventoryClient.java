package com.store.products.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.UUID;
import java.util.Map;

@Slf4j
@Component
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(@Value("${INVENTORY_SERVICE_URL:http://localhost:8081}") @NonNull String baseUrl,
                           @Value("${INVENTORY_API_KEY:inventory-secret-key}") String apiKey) {
        // Sanitize URL to remove trailing slash if present
        String sanitizedUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        log.info("Initializing InventoryClient with URL: {} and API Key present: {}", sanitizedUrl, (apiKey != null && !apiKey.isEmpty()));
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // Aumentamos timeout a 5s por el Cold Start de Render
        factory.setReadTimeout(5000);

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(sanitizedUrl)
                .defaultHeader("x-api-key", apiKey)
                .build();
    }

    @Retry(name = "inventoryService")
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "getAvailableStockFallback")
    public int getAvailableStock(@NonNull UUID productId) {
        try {
            log.debug("Fetching stock for product: {}", productId);
            Map<String, Object> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/inventory/{productId}")
                            .build(productId))
                    .retrieve()
                    .onStatus(status -> status.isError(), (req, resp) -> {
                        log.error("Inventory error status: {} for GET {}", resp.getStatusCode(), productId);
                    })
                    .body(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});
            
            if (response != null && response.get("available") != null) {
                return (Integer) response.get("available");
            }
        } catch (Exception e) {
            log.error("CRITICAL: Failed to fetch stock for {}: {}", productId, e.getMessage());
        }
        return 0;
    }

    @Retry(name = "inventoryService")
    public void initializeStock(UUID productId, int quantity) {
        try {
            log.info("Initializing stock for product: {} with quantity: {}", productId, quantity);
            restClient.put()
                    .uri(uriBuilder -> uriBuilder
                            .path("/inventory/{productId}/stock")
                            .queryParam("quantity", quantity)
                            .build(productId))
                    .retrieve()
                    .onStatus(status -> status.isError(), (req, resp) -> {
                        log.error("Inventory error status: {} for PUT stock {}", resp.getStatusCode(), productId);
                    })
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to initialize stock: {}", e.getMessage());
            throw new RuntimeException("Error comunicando con el servicio de inventario: " + e.getMessage());
        }
    }

    @Retry(name = "inventoryService")
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "deductStockFallback")
    public boolean deductStock(UUID productId, int quantity, String idempotencyKey) {
        try {
            restClient.post()
                    .uri("/inventory/purchases")
                    .header("Idempotency-Key", idempotencyKey)
                    .body(java.util.Objects.requireNonNull(Map.of("productId", productId, "quantity", quantity)))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.error("Purchase failure: {}", e.getMessage());
            return false;
        }
    }

    public boolean deductStockFallback(UUID productId, int quantity, String idempotencyKey, Throwable t) {
        log.error("Circuit breaker for deductStock for product {}: {}", productId, t.getMessage());
        return false;
    }

    public int getAvailableStockFallback(UUID productId, Throwable t) {
        log.error("Circuit breaker for getAvailableStock for product {}: {}", productId, t.getMessage());
        return 0;
    }
}
