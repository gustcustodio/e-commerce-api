package com.gustcustodio.e_commerce_api.repositories;

import com.gustcustodio.e_commerce_api.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


}
