package com.example.store.controller;

import com.example.store.dto.CustomerDTO;
import com.example.store.entity.Customer;
import com.example.store.mapper.CustomerMapper;
import com.example.store.repository.CustomerRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @GetMapping
    public Page<CustomerDTO> getCustomers(Pageable pageable) {
        Pageable pageableWithSort = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id").descending());
        return customerRepository.findAll(pageableWithSort).map(customerMapper::toDto);
    }

    @GetMapping("/search")
    public List<CustomerDTO> searchCustomers(@RequestParam("q") @NotBlank @Size(min = 1) String q) {
        return customerRepository.searchByNameSubstring(q).stream()
                .map(customerMapper::toDto)
                .toList();
    }

    @PostMapping
    public ResponseEntity<Void> createCustomer(@Valid @RequestBody CustomerDTO customerDto) {
        Customer customer = customerMapper.toEntity(customerDto);
        customerRepository.save(customer);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(customer.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }
}
