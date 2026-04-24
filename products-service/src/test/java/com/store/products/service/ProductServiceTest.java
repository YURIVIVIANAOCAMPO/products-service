package com.store.products.service;

import com.store.products.dto.ProductRequestDTO;
import com.store.products.dto.ProductResponseDTO;
import com.store.products.entity.Product;
import com.store.products.entity.ProductStatus;
import com.store.products.exception.DuplicateResourceException;
import com.store.products.exception.InsufficientStockException;
import com.store.products.exception.ResourceNotFoundException;
import com.store.products.mapper.ProductMapper;
import com.store.products.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductRequestDTO requestDTO;
    private ProductResponseDTO responseDTO;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        
        product = Product.builder()
                .id(productId)
                .sku("PROD-001")
                .name("Test Product")
                .price(BigDecimal.valueOf(100.0))
                .status(ProductStatus.ACTIVE)
                .build();

        requestDTO = ProductRequestDTO.builder()
                .sku("PROD-001")
                .name("Test Product")
                .price(BigDecimal.valueOf(100.0))
                .status(ProductStatus.ACTIVE)
                .build();

        responseDTO = ProductResponseDTO.builder()
                .id(productId)
                .sku("PROD-001")
                .name("Test Product")
                .price(BigDecimal.valueOf(100.0))
                .status(ProductStatus.ACTIVE)
                .build();
    }

    @Test
    void createProduct_Success() {
        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(productMapper.toEntity(any(ProductRequestDTO.class))).thenReturn(product);
        when(productRepository.saveAndFlush(any(Product.class))).thenReturn(product);
        when(productMapper.toDTO(any(Product.class))).thenReturn(responseDTO);

        ProductResponseDTO result = productService.createProduct(requestDTO);

        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("PROD-001", result.getSku());
        verify(productRepository).saveAndFlush(any(Product.class));
    }

    @Test
    void createProduct_DuplicateSku_ThrowsException() {
        when(productRepository.existsBySku("PROD-001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> productService.createProduct(requestDTO));
        verify(productRepository, never()).saveAndFlush(any(Product.class));
    }

    @Test
    void purchaseProduct_Success() {
        when(productRepository.existsById(productId)).thenReturn(true);
        when(inventoryClient.deductStock(eq(productId), eq(2), anyString())).thenReturn(true);

        assertDoesNotThrow(() -> productService.purchaseProduct(productId, 2, "key-123"));
    }

    @Test
    void purchaseProduct_InsufficientStock_ThrowsException() {
        when(productRepository.existsById(productId)).thenReturn(true);
        when(inventoryClient.deductStock(eq(productId), eq(2), anyString())).thenReturn(false);

        assertThrows(InsufficientStockException.class, () -> productService.purchaseProduct(productId, 2, "key-124"));
    }

    @Test
    void purchaseProduct_ProductNotFound_ThrowsException() {
        when(productRepository.existsById(productId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> productService.purchaseProduct(productId, 2, "key-125"));
    }
}
