package com.gustcustodio.e_commerce_api.repositories;

import com.gustcustodio.e_commerce_api.entities.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

@DataJpaTest
public class ProductRepositoryTests {

    @Autowired
    private ProductRepository productRepository;

    private Long existingId;
    private Long nonExistingId;
    private PageRequest pageRequest;

    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 1000L;
        pageRequest = PageRequest.of(0, 5);
    }

    @Test
    public void findProductWithCategoriesShouldReturnNotEmptyOptionalWhenIdExists() {
        Optional<Product> result = productRepository.findProductWithCategories(existingId);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertTrue(result.get().getCategories().size() > 0);
    }

    @Test
    public void findProductWithCategoriesShouldReturnEmptyOptionalWhenIdNotExists() {
        Optional<Product> result = productRepository.findProductWithCategories(nonExistingId);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void findAllProductsShouldReturnPageOfProducts() {
        Page<Product> result = productRepository.findAllProducts(pageRequest);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(0, result.getNumber());
        Assertions.assertEquals(5, result.getSize());
        Assertions.assertTrue(result.getTotalElements() > 0);
    }

    @Test
    public void deleteShouldDeleteProductWhenIdExists() {
        productRepository.deleteById(existingId);
        Optional<Product> result = productRepository.findById(existingId);
        Assertions.assertFalse(result.isPresent());
    }

}
