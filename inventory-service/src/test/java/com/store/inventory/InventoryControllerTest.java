package com.store.inventory;

import com.store.inventory.dto.PurchaseRequestDTO;
import com.store.inventory.entity.Inventory;
import com.store.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class InventoryControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InventoryRepository inventoryRepository;

    private UUID productId;

    @BeforeEach
    void setup() {
        inventoryRepository.deleteAll();
        productId = UUID.randomUUID();
        inventoryRepository.save(Inventory.builder()
                .productId(productId)
                .available(10)
                .build());
    }

    @Test
    void testPurchaseSuccess() {
        PurchaseRequestDTO request = new PurchaseRequestDTO();
        request.setProductId(productId);
        request.setQuantity(2);

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", "inventory-secret-key");
        headers.set("Idempotency-Key", "key-123");

        HttpEntity<PurchaseRequestDTO> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("/inventory/purchases", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        Inventory updated = inventoryRepository.findById(productId).orElseThrow();
        assertThat(updated.getAvailable()).isEqualTo(8);
    }

    @Test
    void testPurchaseIdempotency() {
        PurchaseRequestDTO request = new PurchaseRequestDTO();
        request.setProductId(productId);
        request.setQuantity(2);

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", "inventory-secret-key");
        headers.set("Idempotency-Key", "same-key");

        HttpEntity<PurchaseRequestDTO> entity = new HttpEntity<>(request, headers);
        
        // First call
        restTemplate.postForEntity("/inventory/purchases", entity, String.class);
        
        // Second call (duplicate)
        ResponseEntity<String> response = restTemplate.postForEntity("/inventory/purchases", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        Inventory updated = inventoryRepository.findById(productId).orElseThrow();
        assertThat(updated.getAvailable()).isEqualTo(8); // Only deducted once
    }

    @Test
    void testInsufficientStock() {
        PurchaseRequestDTO request = new PurchaseRequestDTO();
        request.setProductId(productId);
        request.setQuantity(100);

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", "inventory-secret-key");

        HttpEntity<PurchaseRequestDTO> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("/inventory/purchases", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
