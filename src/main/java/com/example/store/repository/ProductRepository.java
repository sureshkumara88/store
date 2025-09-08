package com.example.store.repository;

import com.example.store.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @SuppressWarnings("NullableProblems")
    @EntityGraph(attributePaths = {"orders"})
    Page<Product> findAll(Pageable pageable);

    @SuppressWarnings("NullableProblems")
    @EntityGraph(attributePaths = {"orders"})
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findById(@Param("id") Long id);
}
