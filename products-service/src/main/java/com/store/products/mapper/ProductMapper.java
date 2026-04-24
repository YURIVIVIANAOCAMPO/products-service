package com.store.products.mapper;

import com.store.products.dto.ProductRequestDTO;
import com.store.products.dto.ProductResponseDTO;
import com.store.products.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequestDTO requestDTO) {
        if (requestDTO == null) {
            return null;
        }

        return Product.builder()
                .sku(requestDTO.getSku())
                .name(requestDTO.getName())
                .price(requestDTO.getPrice())
                .status(requestDTO.getStatus())
                .build();
    }

    public ProductResponseDTO toDTO(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponseDTO.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .price(product.getPrice())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
