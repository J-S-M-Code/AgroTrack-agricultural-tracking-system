package com.agrotrack.domain.port.out.product;

import com.agrotrack.domain.model.entities.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepositoryPort {
    Product save(Product product);
    Optional<Product> findById(UUID productId);
    boolean existsByNumSenasa(String numSenasa);
    List<Product> findAll();
}