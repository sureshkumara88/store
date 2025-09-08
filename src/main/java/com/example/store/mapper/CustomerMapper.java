package com.example.store.mapper;

import com.example.store.dto.CustomerDTO;
import com.example.store.entity.Customer;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {OrderMapper.class})
public interface CustomerMapper {
    @Mapping(target = "orders", source = "orders")
    CustomerDTO toDto(Customer customer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    Customer toEntity(CustomerDTO customerDto);
}
