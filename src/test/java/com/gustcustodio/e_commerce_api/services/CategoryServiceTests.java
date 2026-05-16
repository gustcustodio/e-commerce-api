package com.gustcustodio.e_commerce_api.services;

import com.gustcustodio.e_commerce_api.constructors.Factory;
import com.gustcustodio.e_commerce_api.dtos.CategoryRequestDTO;
import com.gustcustodio.e_commerce_api.dtos.CategoryResponseDTO;
import com.gustcustodio.e_commerce_api.entities.Category;
import com.gustcustodio.e_commerce_api.repositories.CategoryRepository;
import com.gustcustodio.e_commerce_api.services.exceptions.DatabaseException;
import com.gustcustodio.e_commerce_api.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class CategoryServiceTests {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    private static final String RESOURCE_NOT_FOUND_EXCEPTION_MESSAGE = "Resource not found";
    private static final String DATABASE_EXCEPTION_MESSAGE = "Integrity violation";

    private Long existingId;
    private Long nonExistingId;
    private Long dependentId;
    private Category category;
    private CategoryRequestDTO categoryRequestDTO;
    private PageImpl<Category> page;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 1000L;
        dependentId = 2L;
        category = Factory.createCategory();
        categoryRequestDTO = Factory.createCategoryRequestDTO();
        page = new PageImpl<>(List.of(category));

        when(categoryRepository.findById(existingId)).thenReturn(Optional.of(category));
        when(categoryRepository.findById(nonExistingId)).thenReturn(Optional.empty());
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        doNothing().when(categoryRepository).deleteById(existingId);
        doThrow(DataIntegrityViolationException.class).when(categoryRepository).deleteById(dependentId);
        when(categoryRepository.existsById(existingId)).thenReturn(true);
        when(categoryRepository.existsById(nonExistingId)).thenReturn(false);
        when(categoryRepository.existsById(dependentId)).thenReturn(true);
    }

    @Test
    public void findCategoryByIdShouldReturnCategoryDTOWhenIdExists() {
        CategoryResponseDTO result = categoryService.findCategoryById(existingId);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(category.getId(), result.id());
    }

    @Test
    public void findCategoryByIdShouldReturnResourceNotFoundExceptionWhenIdDoesNotExists() {
        ResourceNotFoundException exception =
                Assertions.assertThrows(ResourceNotFoundException.class, () -> categoryService.findCategoryById(nonExistingId));
        Assertions.assertEquals(RESOURCE_NOT_FOUND_EXCEPTION_MESSAGE, exception.getMessage());
    }

    @Test
    public void findAllCategoriesShouldReturnPage() {
        Page<CategoryResponseDTO> result = categoryService.findAllCategories(0, 5);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(category.getId(), result.getContent().getFirst().id());
    }

    @Test
    public void createCategoryShouldReturnCategoryResponseDTO() {
        CategoryResponseDTO result = categoryService.createCategory(categoryRequestDTO);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(category.getId(), result.id());
        Assertions.assertEquals(categoryRequestDTO.name(), result.name());
    }

    @Test
    public void updateCategoryShouldReturnCategoryResponseDTOWhenIdExists() {
        CategoryResponseDTO result = categoryService.updateCategory(existingId, categoryRequestDTO);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(existingId, result.id());
        Assertions.assertEquals(categoryRequestDTO.name(), category.getName());
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    public void updateCategoryShouldReturnResourceNotFoundExceptionWhenIdDoesNotExists() {
        ResourceNotFoundException exception =
                Assertions.assertThrows(ResourceNotFoundException.class, () -> categoryService.updateCategory(nonExistingId, categoryRequestDTO));
        Assertions.assertEquals(RESOURCE_NOT_FOUND_EXCEPTION_MESSAGE, exception.getMessage());
    }

    @Test
    public void deleteShouldDoNothingWhenIdExists() {
        Assertions.assertDoesNotThrow(() -> categoryService.deleteCategory(existingId));
        verify(categoryRepository, times(1)).deleteById(existingId);
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists(){
        ResourceNotFoundException exception =
                Assertions.assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(nonExistingId));
        Assertions.assertEquals(RESOURCE_NOT_FOUND_EXCEPTION_MESSAGE, exception.getMessage());
        verify(categoryRepository, never()).deleteById(nonExistingId);
    }

    @Test
    public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
        DatabaseException exception =
                Assertions.assertThrows(DatabaseException.class, () -> categoryService.deleteCategory(dependentId));
        Assertions.assertEquals(DATABASE_EXCEPTION_MESSAGE, exception.getMessage());
        verify(categoryRepository, times(1)).deleteById(dependentId);
    }

}


