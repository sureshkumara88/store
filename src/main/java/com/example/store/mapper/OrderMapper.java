package com.example.store.mapper;

import com.example.store.dto.OrderCustomerDTO;
import com.example.store.dto.OrderDTO;
import com.example.store.dto.OrderProductDTO;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {ProductMapper.class})
public interface OrderMapper {

    @Mapping(target = "orders", ignore = true)
    Product toProduct(OrderProductDTO dto);

    @Mapping(target = "orders", ignore = true)
    Customer toCustomer(OrderCustomerDTO dto);

    @Mapping(target = "customer", source = "customer")
    @Mapping(target = "products", source = "products")
    OrderDTO toDto(Order order);

    @Mapping(target = "id", ignore = true)
    Order toEntity(OrderDTO orderDto);
}
