package com.example.store.controller;

import com.example.store.dto.OrderCustomerDTO;
import com.example.store.dto.OrderDTO;
import com.example.store.dto.OrderProductDTO;
import com.example.store.entity.Order;
import com.example.store.mapper.OrderMapper;
import com.example.store.repository.CustomerRepository;
import com.example.store.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@ComponentScan(basePackageClasses = OrderMapper.class)
class OrderControllerTests {

    private static final String BASE_URL = "http://localhost";
    private static final String BASE_API = "/api/v1/orders";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderMapper orderMapper;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private CustomerRepository customerRepository;

    private Order order;
    private OrderDTO dto;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        OrderCustomerDTO customer = new OrderCustomerDTO();
        customer.setId(1L);
        customer.setName("John Doe");

        OrderProductDTO product = new OrderProductDTO();
        product.setId(10L);
        product.setDescription("P1");

        dto = new OrderDTO();
        dto.setDescription("Test Order");
        dto.setId(1L);
        dto.setCustomer(customer);
        dto.setProducts(List.of(product));
    }

    @Test
    void testGetAllOrders() throws Exception {
        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(orderMapper.toDto(any(Order.class))).thenReturn(dto);

        mockMvc.perform(get(BASE_API).param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].description").value("Test Order"))
                .andExpect(jsonPath("$.content[0].customer.name").value("John Doe"));
    }

    @Test
    void testGetOrderById_Found() throws Exception {
        when(orderRepository.findById(eq(1L))).thenReturn(Optional.of(order));
        when(orderMapper.toDto(any(Order.class))).thenReturn(dto);

        mockMvc.perform(get(BASE_API + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customer.name").value("John Doe"));
    }

    @Test
    void testGetOrderById_NotFound() throws Exception {
        when(orderRepository.findById(eq(42L))).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_API + "/42")).andExpect(status().isNotFound());
    }

    @Test
    void testCreateOrder_Success() throws Exception {
        OrderDTO payload = new OrderDTO();
        payload.setDescription("New Order");
        OrderCustomerDTO c = new OrderCustomerDTO();
        c.setId(1L);
        c.setName("John");
        payload.setCustomer(c);
        OrderProductDTO p = new OrderProductDTO();
        p.setId(10L);
        p.setDescription("P1");
        payload.setProducts(List.of(p));

        when(orderMapper.toEntity(any(OrderDTO.class))).thenReturn(order);

        mockMvc.perform(post(BASE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(LOCATION))
                .andExpect(header().string(LOCATION, BASE_URL + BASE_API + "/1"))
                .andExpect(content().string(""));
    }

    @Test
    void testCreateOrder_EmptyDescription() throws Exception {
        OrderDTO payload = new OrderDTO();
        OrderCustomerDTO customer = new OrderCustomerDTO();
        customer.setName("Nick");
        payload.setCustomer(customer);
        payload.setDescription("");
        payload.setProducts(List.of(new OrderProductDTO()));

        mockMvc.perform(post(BASE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].message").value("description is required"));
    }

    @Test
    void testCreateOrder_EmptyCustomer() throws Exception {
        OrderDTO payload = new OrderDTO();
        payload.setDescription("Ice-cream");
        payload.setProducts(List.of(new OrderProductDTO()));

        mockMvc.perform(post(BASE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].message").value("customer is required"));
    }

    @Test
    void testCreateOrder_EmptyProducts() throws Exception {
        OrderDTO payload = new OrderDTO();
        OrderCustomerDTO customer = new OrderCustomerDTO();
        customer.setName("Nick");
        payload.setCustomer(customer);
        payload.setDescription("Ice-cream");
        payload.setProducts(List.of());

        mockMvc.perform(post(BASE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].message").value("Order must contain at least one product"));
    }

    @Test
    void testCreateOrder_AllEmpty() throws Exception {
        OrderDTO payload = new OrderDTO();
        payload.setDescription("");
        payload.setProducts(List.of());

        mockMvc.perform(post(BASE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[*].message")
                        .value(containsInAnyOrder(
                                "customer is required",
                                "description is required",
                                "Order must contain at least one product")));
    }

    @Test
    void testCreateOrder_InvalidIds_DataIntegrityViolation() throws Exception {
        OrderDTO payload = new OrderDTO();
        OrderCustomerDTO customer = new OrderCustomerDTO();
        customer.setName("Nick");
        payload.setCustomer(customer);
        payload.setDescription("X");
        OrderProductDTO p = new OrderProductDTO();
        p.setId(999L);
        p.setDescription("Bad");
        payload.setProducts(List.of(p));

        when(orderMapper.toEntity(any(OrderDTO.class))).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenThrow(new DataIntegrityViolationException("bad fk"));

        mockMvc.perform(post(BASE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message").value("Request body contains invalid or missing customer/product id's"));
    }

    @Test
    void testCreateOrder_InvalidRequest_DataAccessException() throws Exception {
        OrderDTO payload = new OrderDTO();
        OrderCustomerDTO customer = new OrderCustomerDTO();
        customer.setName("Nick");
        payload.setCustomer(customer);
        payload.setDescription("X");
        OrderProductDTO p = new OrderProductDTO();
        p.setDescription("Bad");
        payload.setProducts(List.of(p));

        when(orderMapper.toEntity(any(OrderDTO.class))).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenThrow(new ConcurrencyFailureException("concurrency error"));

        mockMvc.perform(post(BASE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.message").value("concurrency error"));
    }
}
