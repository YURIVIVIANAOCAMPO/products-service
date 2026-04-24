package com.store.products.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.products.dto.ProductRequestDTO;
import com.store.products.entity.ProductStatus;
import com.store.products.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers
@SuppressWarnings({"resource", "null"})
class ProductControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void shouldCreateProductSuccessfully() throws Exception {
        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .sku("PROD-INT-001")
                .name("Integration Test Product")
                .price(BigDecimal.valueOf(50.0))
                .status(ProductStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.sku", is("PROD-INT-001")))
                .andExpect(jsonPath("$.data.name", is("Integration Test Product")));

        assertTrue(productRepository.existsBySku("PROD-INT-001"));
    }

    @Test
    void shouldReturn422WhenPriceIsNegative() throws Exception {
        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .sku("PROD-INT-002")
                .name("Bad Product")
                .price(BigDecimal.valueOf(-10.0))
                .status(ProductStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0].detail", notNullValue()));
    }
}
