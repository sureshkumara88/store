package com.example.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.util.List;

@Data
public class OrderDTO {
    private Long id;

    @NotBlank(message = "description is required")
    private String description;

    @NotNull(message = "customer is required") private OrderCustomerDTO customer;

    @NotEmpty(message = "Order must contain at least one product")
    private List<OrderProductDTO> products;
}
