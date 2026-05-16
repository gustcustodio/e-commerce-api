package com.gustcustodio.e_commerce_api.controllers;

import com.gustcustodio.e_commerce_api.dtos.CategoryRequestDTO;
import com.gustcustodio.e_commerce_api.dtos.CategoryResponseDTO;
import com.gustcustodio.e_commerce_api.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(value = "/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<CategoryResponseDTO> findCategoryById(@PathVariable Long id) {
        CategoryResponseDTO dto = categoryService.findCategoryById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<Page<CategoryResponseDTO>> findAllCategories(@RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
                                                                        @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        Page<CategoryResponseDTO> page = categoryService.findAllCategories(pageNumber, pageSize);
        return ResponseEntity.ok(page);
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryRequestDTO categoryRequestDTO) {
        CategoryResponseDTO categoryResponseDTO = categoryService.createCategory(categoryRequestDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(categoryResponseDTO.id()).toUri();
        return ResponseEntity.created(uri).body(categoryResponseDTO);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequestDTO categoryRequestDTO) {
        CategoryResponseDTO categoryResponseDTO = categoryService.updateCategory(id, categoryRequestDTO);
        return ResponseEntity.ok(categoryResponseDTO);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

}
