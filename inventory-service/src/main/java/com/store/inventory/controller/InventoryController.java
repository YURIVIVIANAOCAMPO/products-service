package com.store.inventory.controller;

import com.store.inventory.dto.PurchaseRequestDTO;
import com.store.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Inventory Management", description = "Endpoints for managing physical stock levels and stock deductions")
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(
        summary = "Get available stock",
        description = "Retrieves the current available quantity for a specific product ID.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Stock information retrieved")
        }
    )
    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> getStock(
            @Parameter(description = "UUID of the product to check", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable @NonNull UUID productId) {
        Integer available = inventoryService.getAvailableStock(productId);
        return ResponseEntity.ok(Map.of("productId", productId, "available", available));
    }

    @Operation(
        summary = "Initialize or update stock",
        description = "Sets or adds to the available stock for a product. This is typically called when a product is first created or when new merchandise arrives.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Stock updated successfully")
        }
    )
    @PutMapping("/{productId}/stock")
    public ResponseEntity<Void> initializeStock(
            @Parameter(description = "UUID of the product", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable @NonNull UUID productId,
            @Parameter(description = "Quantity of items arriving in inventory", example = "100")
            @RequestParam @NonNull Integer quantity) {
        inventoryService.initializeStock(productId, quantity);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Process a purchase deduction",
        description = "Deducts a specified quantity from a product's stock. Requires an Idempotency-Key header to ensure safety during network retries.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Stock deducted successfully"),
            @ApiResponse(responseCode = "400", description = "Insufficient stock available"),
            @ApiResponse(responseCode = "409", description = "Conflict or optimistic locking failure")
        }
    )
    @PostMapping("/purchases")
    public ResponseEntity<Map<String, String>> purchase(
            @Valid @RequestBody PurchaseRequestDTO request,
            @Parameter(description = "Unique key for idempotency to avoid duplicate processing", example = "req-12345")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        
        inventoryService.purchase(request, idempotencyKey);
        return ResponseEntity.ok(Map.of("message", "Purchase successful"));
    }
}
