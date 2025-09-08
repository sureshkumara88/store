package com.example.store.controller;

import com.example.store.dto.ProductDTO;
import com.example.store.entity.Product;
import com.example.store.mapper.ProductMapper;
import com.example.store.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class)
@ComponentScan(basePackageClasses = ProductMapper.class)
class ProductControllerTests {

    private static final String BASE_URL = "http://localhost";
    private static final String BASE_API = "/api/v1/products";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private ProductMapper productMapper;

    private Product entity;
    private ProductDTO dto;

    @BeforeEach
    void setUp() {
        entity = new Product();
        entity.setId(1L);
        dto = new ProductDTO();
        dto.setId(7L);
        dto.setDescription("Widget");
    }

    @Test
    void testGetAllProducts() throws Exception {
        Page<Product> page = new PageImpl<>(List.of(entity));
        when(productRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(productMapper.toDto(any(Product.class))).thenReturn(dto);

        mockMvc.perform(get(BASE_API).param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(7))
                .andExpect(jsonPath("$.content[0].description").value("Widget"));
    }

    @Test
    void testGetProductById_Found() throws Exception {
        when(productRepository.findById(eq(7L))).thenReturn(Optional.of(entity));
        when(productMapper.toDto(any(Product.class))).thenReturn(dto);

        mockMvc.perform(get(BASE_API + "/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.description").value("Widget"));
    }

    @Test
    void testGetProductById_NotFound() throws Exception {
        when(productRepository.findById(eq(404L))).thenReturn(Optional.empty());
        mockMvc.perform(get(BASE_API + "/404")).andExpect(status().isNotFound());
    }

    @Test
    void testCreateProduct_Success() throws Exception {
        ProductDTO payload = new ProductDTO();
        payload.setDescription("Widget");

        when(productMapper.toEntity(any(ProductDTO.class))).thenReturn(entity);

        mockMvc.perform(post(BASE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(LOCATION))
                .andExpect(header().string(LOCATION, BASE_URL + BASE_API + "/1"))
                .andExpect(content().string(""));
    }

    @Test
    void testCreateProduct_EmptyDescription() throws Exception {
        ProductDTO payload = new ProductDTO();
        payload.setDescription("");

        mockMvc.perform(post(BASE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].message").value("description is required"));
    }

    @Test
    void testCreateProduct_InvalidRequest_DataAccessException() throws Exception {
        ProductDTO payload = new ProductDTO();
        payload.setDescription("Widget");

        when(productMapper.toEntity(any(ProductDTO.class))).thenReturn(entity);
        when(productRepository.save(any(Product.class))).thenThrow(new DataIntegrityViolationException("Bad pk"));

        mockMvc.perform(post(BASE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.message").value("Bad pk"));
    }
}
