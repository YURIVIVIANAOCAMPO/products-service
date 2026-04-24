package com.store.products.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(@Value("${inventory.service.url:http://localhost:8081}") @NonNull String baseUrl,
                           @Value("${inventory.service.api-key:default-secret-key}") String apiKey) {
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
    public int getAvailableStock(@NonNull String sku) {
        Integer stock = restClient.get()
                .uri("/api/inventory/{sku}/stock", sku)
                .retrieve()
                .body(Integer.class);
        return stock != null ? stock : 0;
    }

    public int getAvailableStockFallback(String sku, Throwable t) {
        return 0;
    }

    @Retry(name = "inventoryService")
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "deductStockFallback")
    public boolean deductStock(String sku, int quantity) {
        restClient.post()
                .uri("/api/inventory/deduct")
                .body(new StockDeductionRequest(sku, quantity))
                .retrieve()
                .toBodilessEntity();
        return true;
    }

    public boolean deductStockFallback(String sku, int quantity, Throwable t) {
        return false;
    }

    // Inner class for the request body
    public record StockDeductionRequest(String sku, int quantity) {}
}
