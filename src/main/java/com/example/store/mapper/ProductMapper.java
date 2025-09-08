package com.example.store.mapper;

import com.example.store.dto.ProductDTO;
import com.example.store.entity.Order;
import com.example.store.entity.Product;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "orderIds", source = "orders")
    ProductDTO toDto(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    Product toEntity(ProductDTO productDto);

    default List<Long> mapOrderToIds(List<Order> orders) {
        List<Long> result;
        if (orders != null) {
            result = orders.stream().map(Order::getId).toList();
        } else {
            result = Collections.emptyList();
        }
        return result;
    }
}
