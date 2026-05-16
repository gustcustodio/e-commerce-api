package com.gustcustodio.e_commerce_api.services;

import com.gustcustodio.e_commerce_api.dtos.CategoryRequestDTO;
import com.gustcustodio.e_commerce_api.dtos.CategoryResponseDTO;
import com.gustcustodio.e_commerce_api.entities.Category;
import com.gustcustodio.e_commerce_api.repositories.CategoryRepository;
import com.gustcustodio.e_commerce_api.services.exceptions.DatabaseException;
import com.gustcustodio.e_commerce_api.services.exceptions.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO findCategoryById(Long id) {
        Category entity = categoryRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        return new CategoryResponseDTO(entity);
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponseDTO> findAllCategories(Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        Page<Category> page = categoryRepository.findAll(pageRequest);
        return page.map(CategoryResponseDTO::new);
    }

    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO) {
        Category entity = new Category();
        convertDtoToEntity(categoryRequestDTO, entity);
        entity = categoryRepository.save(entity);
        return new CategoryResponseDTO(entity);
    }

    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO categoryRequestDTO) {
        Category entity = categoryRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        convertDtoToEntity(categoryRequestDTO, entity);
        entity = categoryRepository.save(entity);
        return new CategoryResponseDTO(entity);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) throw new ResourceNotFoundException();
        try {
            categoryRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException();
        }
    }

    private void convertDtoToEntity(CategoryRequestDTO categoryRequestDTO, Category entity) {
        entity.setName(categoryRequestDTO.name());
    }

}
