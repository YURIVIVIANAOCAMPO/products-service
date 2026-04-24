package com.store.products.service;

import com.store.products.dto.ProductRequestDTO;
import com.store.products.dto.ProductResponseDTO;
import com.store.products.entity.Product;
import com.store.products.exception.DuplicateResourceException;
import com.store.products.exception.InsufficientStockException;
import com.store.products.exception.ResourceNotFoundException;
import com.store.products.mapper.ProductMapper;
import com.store.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.lang.NonNull;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final InventoryClient inventoryClient;

    @Transactional
    public void purchaseProduct(@NonNull UUID id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        String sku = java.util.Objects.requireNonNull(product.getSku(), "Product SKU cannot be null");
        
        int availableStock = inventoryClient.getAvailableStock(sku);
        if (availableStock < quantity) {
            throw new InsufficientStockException("Not enough stock available. Current stock: " + availableStock);
        }
        
        boolean success = inventoryClient.deductStock(sku, quantity);
        if (!success) {
            throw new InsufficientStockException("Failed to deduct stock for product: " + product.getSku());
        }
    }

    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {
        validateSkuUniqueness(requestDTO.getSku());

        Product product = java.util.Objects.requireNonNull(productMapper.toEntity(requestDTO), "Product entity cannot be null");
        Product savedProduct = productRepository.saveAndFlush(product);

        return productMapper.toDTO(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(@NonNull UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productMapper.toDTO(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> listProducts(String search, com.store.products.entity.ProductStatus status, @NonNull Pageable pageable) {
        Page<Product> productPage = productRepository.findByFilters(status, search, pageable);
        return productPage.map(productMapper::toDTO);
    }

    @Transactional
    public ProductResponseDTO updateProduct(@NonNull UUID id, ProductRequestDTO requestDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Check if SKU changed and if the new SKU is already taken
        if (!product.getSku().equals(requestDTO.getSku())) {
            validateSkuUniqueness(requestDTO.getSku());
        }

        product.setSku(requestDTO.getSku());
        product.setName(requestDTO.getName());
        product.setPrice(requestDTO.getPrice());
        product.setStatus(requestDTO.getStatus());

        return productMapper.toDTO(product);
    }

    @Transactional
    public void deleteProduct(@NonNull UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    private void validateSkuUniqueness(String sku) {
        if (productRepository.existsBySku(sku)) {
            throw new DuplicateResourceException("Product already exists with SKU: " + sku);
        }
    }
}
