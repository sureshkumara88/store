package com.example.store.controller;

import com.example.store.dto.CustomerDTO;
import com.example.store.entity.Customer;
import com.example.store.mapper.CustomerMapper;
import com.example.store.repository.CustomerRepository;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@ComponentScan(basePackageClasses = CustomerMapper.class)
class CustomerControllerTests {

    private static final String BASE_URL = "http://localhost";
    private static final String BASE_API = "/api/v1/customers";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerRepository customerRepository;

    @MockitoBean
    private CustomerMapper customerMapper;

    private Customer customer;
    private CustomerDTO dto;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setName("John Doe");
        customer.setId(1L);

        dto = new CustomerDTO();
        dto.setId(1L);
        dto.setName("John Doe");
    }

    @Test
    void testGetAllCustomers() throws Exception {
        Page<Customer> page = new PageImpl<>(List.of(customer));
        when(customerRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(customerMapper.toDto(any(Customer.class))).thenReturn(dto);

        mockMvc.perform(get(BASE_API).param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("John Doe"));
    }

    @Test
    void testSearchCustomers() throws Exception {
        when(customerRepository.searchByNameSubstring(eq("john"))).thenReturn(List.of(this.customer));
        when(customerMapper.toDto(customer)).thenReturn(dto);

        mockMvc.perform(get(BASE_API + "/search").param("q", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    void testCreateCustomer_Success() throws Exception {
        CustomerDTO payload = new CustomerDTO();
        payload.setName("Alice");

        when(customerMapper.toEntity(any(CustomerDTO.class))).thenReturn(customer);

        mockMvc.perform(post(BASE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(LOCATION))
                .andExpect(header().string(LOCATION, BASE_URL + BASE_API + "/1"))
                .andExpect(content().string("")); // void controller -> no body
    }

    @Test
    void testCreateCustomer_EmptyName_BadRequest() throws Exception {
        CustomerDTO payload = new CustomerDTO();
        payload.setName("");

        mockMvc.perform(post(BASE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].message").value("name is required"));
    }

    @Test
    void testCreateCustomer_InvalidRequest_DataAccessException() throws Exception {
        CustomerDTO payload = new CustomerDTO();
        payload.setName("Nick");

        when(customerMapper.toEntity(any(CustomerDTO.class))).thenReturn(customer);
        when(customerRepository.save(any(Customer.class))).thenThrow(new DataIntegrityViolationException("Bad pk"));

        mockMvc.perform(post(BASE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.error").value("data_access_error"))
                .andExpect(jsonPath("$.message").value("Bad pk"));
    }

    @Test
    void testCreateCustomer_InvalidRequest_InternalServerError() throws Exception {
        CustomerDTO payload = new CustomerDTO();
        payload.setName("Nick");

        when(customerMapper.toEntity(any(CustomerDTO.class))).thenThrow(new RuntimeException("server error"));

        mockMvc.perform(post(BASE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.error").value("unexpected_error"))
                .andExpect(jsonPath("$.message").value("server error"));
    }
}
