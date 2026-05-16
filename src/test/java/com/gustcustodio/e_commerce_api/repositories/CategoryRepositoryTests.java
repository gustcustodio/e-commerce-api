package com.gustcustodio.e_commerce_api.repositories;

import com.gustcustodio.e_commerce_api.entities.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

@DataJpaTest
public class CategoryRepositoryTests {

    @Autowired
    private CategoryRepository categoryRepository;

    private Long existingId;
    private Long nonExistingId;
    private PageRequest pageRequest;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 1000L;
        pageRequest = PageRequest.of(0, 5);
    }

    @Test
    public void findByIdShouldReturnNotEmptyOptionalWhenIdExists() {
        Optional<Category> result = categoryRepository.findById(existingId);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(existingId, result.get().getId());
    }

    @Test
    public void findByIdShouldReturnEmptyOptionalWhenIdNotExists() {
        Optional<Category> result = categoryRepository.findById(nonExistingId);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void findAllShouldReturnPageOfCategories() {
        Page<Category> result = categoryRepository.findAll(pageRequest);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(0, result.getNumber());
        Assertions.assertEquals(5, result.getSize());
        Assertions.assertTrue(result.getTotalElements() > 0);
    }

    @Test
    public void deleteShouldDeleteObjectWhenIdExists() {
        categoryRepository.deleteById(existingId);
        Optional<Category> result = categoryRepository.findById(existingId);
        Assertions.assertFalse(result.isPresent());
    }

}


