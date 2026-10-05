package com.fatih.microservices.product.repository;

import com.fatih.microservices.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository
        extends JpaRepository<Product, Long> {
}