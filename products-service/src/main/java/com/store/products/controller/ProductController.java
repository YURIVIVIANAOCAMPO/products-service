package com.store.products.controller;

import com.store.products.dto.ProductRequestDTO;
import com.store.products.dto.ProductResponseDTO;
import com.store.products.dto.PurchaseRequestDTO;
import com.store.products.dto.ResponseWrapper;
import com.store.products.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ResponseWrapper<ProductResponseDTO>> createProduct(@Valid @RequestBody ProductRequestDTO requestDTO) {
        ProductResponseDTO createdProduct = productService.createProduct(requestDTO);
        return new ResponseEntity<>(new ResponseWrapper<>(createdProduct), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ResponseWrapper<Page<ProductResponseDTO>>> listProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) com.store.products.entity.ProductStatus status,
            @NonNull Pageable pageable) {
        return ResponseEntity.ok(new ResponseWrapper<>(productService.listProducts(search, status, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ProductResponseDTO>> getProductById(@PathVariable @NonNull UUID id) {
        return ResponseEntity.ok(new ResponseWrapper<>(productService.getProductById(id)));
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<Void> purchaseProduct(
            @PathVariable @NonNull UUID id, 
            @Valid @RequestBody PurchaseRequestDTO requestDTO) {
        productService.purchaseProduct(id, requestDTO.getQuantity());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ProductResponseDTO>> updateProduct(
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody ProductRequestDTO requestDTO) {
        return ResponseEntity.ok(new ResponseWrapper<>(productService.updateProduct(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable @NonNull UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
