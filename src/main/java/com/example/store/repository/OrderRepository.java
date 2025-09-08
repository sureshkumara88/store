package com.example.store.repository;

import com.example.store.entity.Order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @SuppressWarnings("NullableProblems")
    @EntityGraph(attributePaths = {"customer", "products"})
    Page<Order> findAll(Pageable pageable);

    @SuppressWarnings("NullableProblems")
    @EntityGraph(attributePaths = {"customer", "products"})
    Optional<Order> findById(Long id);
}
