package com.gustcustodio.e_commerce_api.repositories;

import com.gustcustodio.e_commerce_api.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT obj FROM Product obj JOIN FETCH obj.categories WHERE obj.id = :id")
    Optional<Product> findProductWithCategories(Long id);

    @Query(value = "SELECT obj FROM Product obj LEFT JOIN FETCH obj.categories", countQuery = "SELECT COUNT(obj) FROM Product obj")
    Page<Product> findAllProducts(Pageable pageable);

}
