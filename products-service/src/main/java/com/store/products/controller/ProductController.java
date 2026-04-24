package com.store.products.controller;

import com.store.products.dto.ProductRequestDTO;
import com.store.products.dto.ProductResponseDTO;
import com.store.products.dto.ResponseWrapper;
import com.store.products.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "Endpoints for managing the product catalog and processing purchases")
public class ProductController {

    private final ProductService productService;

    @Operation(
        summary = "Create a new product",
        description = "Registers a new product in the catalog. Includes metadata and optional initial stock initialization in the inventory service.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Product with the same SKU already exists")
        }
    )
    @PostMapping
    public ResponseEntity<ResponseWrapper<ProductResponseDTO>> createProduct(
            @Parameter(description = "Product details including name, price, SKU and initial stock", required = true)
            @Valid @RequestBody ProductRequestDTO requestDTO) {
        ProductResponseDTO createdProduct = productService.createProduct(requestDTO);
        return new ResponseEntity<>(new ResponseWrapper<>(createdProduct), HttpStatus.CREATED);
    }

    @Operation(
        summary = "List all products with filtering",
        description = "Retrieves a paginated list of products. Supports searching by name or SKU and filtering by status.",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of products retrieved successfully")
        }
    )
    @GetMapping
    public ResponseEntity<ResponseWrapper<Page<ProductResponseDTO>>> listProducts(
            @Parameter(description = "Search term for product name or SKU (e.g., 'LAP-001' or 'Laptop')")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by product status (ACTIVE, INACTIVE)")
            @RequestParam(required = false) com.store.products.entity.ProductStatus status,
            @org.springdoc.core.annotations.ParameterObject @org.springframework.data.web.PageableDefault(page = 0, size = 10) @org.springframework.lang.NonNull org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(new ResponseWrapper<>(productService.listProducts(search, status, java.util.Objects.requireNonNull(pageable))));
    }

    @Operation(
        summary = "Get product by ID",
        description = "Retrieves detailed information of a single product including its current real-time stock from the inventory service.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ProductResponseDTO>> getProductById(
            @Parameter(description = "Unique UUID of the product", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable @NonNull UUID id) {
        return ResponseEntity.ok(new ResponseWrapper<>(productService.getProductById(id)));
    }



    @Operation(
        summary = "Update an existing product",
        description = "Updates metadata (name, price, SKU, status) for an existing product.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ProductResponseDTO>> updateProduct(
            @Parameter(description = "ID of the product to update")
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody ProductRequestDTO requestDTO) {
        return ResponseEntity.ok(new ResponseWrapper<>(productService.updateProduct(id, requestDTO)));
    }

    @Operation(
        summary = "Sync product status",
        description = "Internal endpoint used by the Inventory Service to update product availability status (ACTIVE/INACTIVE) based on current stock levels.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Status synchronized successfully")
        }
    )
    @PatchMapping("/{id}/sync-status")
    public ResponseEntity<Void> syncStatus(
            @PathVariable @NonNull UUID id,
            @RequestParam int stock) {
        productService.updateProductStatus(id, stock);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Delete a product",
        description = "Removes a product from the catalog. Note: This does not automatically remove the inventory record.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable @NonNull UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Purchase a product",
        description = "Processes a purchase for the specified product. Uses an Idempotency-Key header to prevent duplicate transactions.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Purchase processed successfully"),
            @ApiResponse(responseCode = "400", description = "Insufficient stock"),
            @ApiResponse(responseCode = "404", description = "Product not found")
        }
    )
    @PostMapping(value = "/{id}/purchase", consumes = org.springframework.http.MediaType.ALL_VALUE)
    public ResponseEntity<ResponseWrapper<String>> purchaseProduct(
            @PathVariable @NonNull UUID id,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        productService.purchaseProduct(id, quantity, idempotencyKey);
        return ResponseEntity.ok(new ResponseWrapper<>("Purchase successful"));
    }

    @Operation(
        summary = "Update product stock",
        description = "Updates the physical stock level for a product. This endpoint bridges to the Inventory Service.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Stock updated successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
        }
    )
    @PutMapping("/{id}/stock")
    public ResponseEntity<ResponseWrapper<String>> updateStock(
            @PathVariable @NonNull UUID id,
            @RequestParam int quantity) {
        productService.updateStock(id, quantity);
        return ResponseEntity.ok(new ResponseWrapper<>("Stock updated successfully"));
    }
}
