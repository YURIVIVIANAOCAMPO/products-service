package com.store.inventory.controller;

import com.store.inventory.dto.PurchaseRequestDTO;
import com.store.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/purchases")
    public ResponseEntity<Map<String, String>> purchase(
            @Valid @RequestBody PurchaseRequestDTO request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        
        inventoryService.purchase(request, idempotencyKey);
        return ResponseEntity.ok(Map.of("message", "Purchase successful"));
    }
}
