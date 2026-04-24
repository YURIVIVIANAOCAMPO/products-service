package com.store.inventory.service;

import com.store.inventory.dto.PurchaseRequestDTO;
import com.store.inventory.entity.IdempotencyKey;
import com.store.inventory.entity.Inventory;
import com.store.inventory.exception.InsufficientStockException;
import com.store.inventory.repository.IdempotencyKeyRepository;
import com.store.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ProductsClient productsClient;

    @Transactional(readOnly = true)
    public Integer getAvailableStock(@org.springframework.lang.NonNull UUID productId) {
        return inventoryRepository.findById(productId)
                .map(Inventory::getAvailable)
                .orElse(0);
    }

    @Transactional
    public void initializeStock(@org.springframework.lang.NonNull UUID productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findById(productId)
                .orElse(Inventory.builder()
                        .productId(productId)
                        .available(0)
                        .version(0L)
                        .build());
        
        inventory.setAvailable(inventory.getAvailable() + (quantity != null ? quantity : 0));
        inventoryRepository.save(java.util.Objects.requireNonNull(inventory));
        log.info("InventoryInitialized: Product {} initial stock set to {}", productId, inventory.getAvailable());
        
        // Sync status back to Products Service
        productsClient.syncStatus(productId, inventory.getAvailable());
    }

    @Transactional
    public void purchase(PurchaseRequestDTO request, String idempotencyKey) {
        // 1. Validate Idempotency
        if (idempotencyKey != null && idempotencyKeyRepository.existsById(idempotencyKey)) {
            log.info("Duplicate request detected for idempotency key: {}", idempotencyKey);
            return; // Idempotent: already processed
        }

        // 2. Fetch and Validate Stock
        UUID productId = java.util.Objects.requireNonNull(request.getProductId(), "Product ID cannot be null");
        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product inventory not found"));

        if (inventory.getAvailable() < request.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock for product: " + productId);
        }

        // 3. Deduct Stock (Optimistic Locking will be handled by @Version)
        inventory.setAvailable(inventory.getAvailable() - request.getQuantity());
        inventoryRepository.save(inventory);

        // 4. Save Idempotency Key
        if (idempotencyKey != null) {
            IdempotencyKey key = IdempotencyKey.builder()
                    .idempotencyKey(idempotencyKey)
                    .createdAt(LocalDateTime.now())
                    .build();
            idempotencyKeyRepository.save(java.util.Objects.requireNonNull(key));
        }

        // 5. Log Event (Required by tech test)
        log.info("InventoryChanged: Product {} new available stock is {}", inventory.getProductId(), inventory.getAvailable());
        
        // 6. Sync status back to Products Service
        productsClient.syncStatus(java.util.Objects.requireNonNull(inventory.getProductId()), inventory.getAvailable());
    }
}
