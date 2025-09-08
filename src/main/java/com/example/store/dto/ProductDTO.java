package com.example.store.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

import java.util.List;

@Data
public class ProductDTO {
    private Long id;

    @NotBlank(message = "description is required")
    private String description;

    private List<Long> orderIds;
}
