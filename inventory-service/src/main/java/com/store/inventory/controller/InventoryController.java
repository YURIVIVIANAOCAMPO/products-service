package com.store.inventory.controller;

import com.store.inventory.dto.PurchaseRequestDTO;
import com.store.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> getStock(@PathVariable UUID productId) {
        Integer available = inventoryService.getAvailableStock(productId);
        return ResponseEntity.ok(Map.of("productId", productId, "available", available));
    }

    @PutMapping("/{productId}/stock")
    public ResponseEntity<Void> initializeStock(
            @PathVariable @NonNull UUID productId,
            @RequestParam @NonNull Integer quantity) {
        inventoryService.initializeStock(productId, quantity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/purchases")
    public ResponseEntity<Map<String, String>> purchase(
            @Valid @RequestBody PurchaseRequestDTO request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        
        inventoryService.purchase(request, idempotencyKey);
        return ResponseEntity.ok(Map.of("message", "Purchase successful"));
    }
}
