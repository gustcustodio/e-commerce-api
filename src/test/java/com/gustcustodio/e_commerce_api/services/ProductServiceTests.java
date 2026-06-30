package com.gustcustodio.e_commerce_api.services;

import com.gustcustodio.e_commerce_api.factories.CategoryFactory;
import com.gustcustodio.e_commerce_api.factories.ProductFactory;
import com.gustcustodio.e_commerce_api.dtos.CategoryResponseDTO;
import com.gustcustodio.e_commerce_api.dtos.ProductRequestDTO;
import com.gustcustodio.e_commerce_api.dtos.ProductResponseDTO;
import com.gustcustodio.e_commerce_api.entities.Category;
import com.gustcustodio.e_commerce_api.entities.Product;
import com.gustcustodio.e_commerce_api.repositories.CategoryRepository;
import com.gustcustodio.e_commerce_api.repositories.ProductRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private static final String RESOURCE_NOT_FOUND_EXCEPTION_MESSAGE = "Resource not found";
    private static final String DATABASE_EXCEPTION_MESSAGE = "Integrity violation";

    private Long existingId;
    private Long nonExistingId;
    private Long dependentId;
    private Product product;
    private ProductRequestDTO productRequestDTO;
    private Category category;
    private CategoryResponseDTO categoryResponseDTO;
    private PageImpl<Product> page;

    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 1000L;
        dependentId = 2L;
        product = ProductFactory.createValidProduct();
        productRequestDTO = ProductFactory.createProductRequestDTO();
        category = CategoryFactory.createCategory();
        categoryResponseDTO = CategoryFactory.createCategoryResponseDTO();
        page = new PageImpl<>(List.of(product));

        when(productRepository.findProductWithCategories(existingId)).thenReturn(Optional.of(product));
        when(productRepository.findProductWithCategories(nonExistingId)).thenReturn(Optional.empty());
        when(productRepository.findAllProducts(any(Pageable.class))).thenReturn(page);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(categoryRepository.getReferenceById(anyLong())).thenReturn(category);
        doNothing().when(productRepository).deleteById(existingId);
        doThrow(DataIntegrityViolationException.class).when(productRepository).deleteById(dependentId);
        when(productRepository.existsById(existingId)).thenReturn(true);
        when(productRepository.existsById(nonExistingId)).thenReturn(false);
        when(productRepository.existsById(dependentId)).thenReturn(true);
    }

    @Test
    public void findProductWithCategoriesShouldReturnProductDTOWhenIdExists() {
        ProductResponseDTO result = productService.findProductWithCategories(existingId);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(product.getId(), result.id());
    }

    @Test
    public void findProductWithCategoriesShouldReturnResourceNotFoundExceptionWhenIdDoesNotExists() {
        ResourceNotFoundException exception =
                Assertions.assertThrows(ResourceNotFoundException.class, () -> productService.findProductWithCategories(nonExistingId));
        Assertions.assertEquals(RESOURCE_NOT_FOUND_EXCEPTION_MESSAGE, exception.getMessage());
    }

    @Test
    public void findAllProductsShouldReturnPage() {
        Page<ProductResponseDTO> result = productService.findAllProducts(0, 5);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(product.getId(), result.getContent().getFirst().id());
    }

    @Test
    public void createProductShouldReturnProductResponseDTO() {
        ProductResponseDTO result = productService.createProduct(productRequestDTO);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(product.getId(), result.id());
        Assertions.assertFalse(result.categories().isEmpty());
    }

    @Test
    public void updateProductShouldReturnProductResponseDTOWhenIdExists() {
        ProductResponseDTO result = productService.updateProduct(existingId, productRequestDTO);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(existingId, result.id());
        Assertions.assertEquals(productRequestDTO.name(), product.getName());
        Assertions.assertTrue(result.categories().contains(categoryResponseDTO));
        verify(productRepository, times(1)).save(product);
    }

    @Test
    public void updateProductShouldReturnResourceNotFoundExceptionWhenIdDoesNotExists() {
        ResourceNotFoundException exception =
                Assertions.assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(nonExistingId, productRequestDTO));
        Assertions.assertEquals(RESOURCE_NOT_FOUND_EXCEPTION_MESSAGE, exception.getMessage());
    }

    @Test
    public void deleteShouldDoNothingWhenIdExists() {
        Assertions.assertDoesNotThrow(() -> productService.deleteProduct(existingId));
        verify(productRepository, times(1)).deleteById(existingId);
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists(){
        ResourceNotFoundException exception =
                Assertions.assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(nonExistingId));
        Assertions.assertEquals(RESOURCE_NOT_FOUND_EXCEPTION_MESSAGE, exception.getMessage());
        verify(productRepository, never()).deleteById(nonExistingId);
    }

    @Test
    public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
        DatabaseException exception =
                Assertions.assertThrows(DatabaseException.class, () -> productService.deleteProduct(dependentId));
        Assertions.assertEquals(DATABASE_EXCEPTION_MESSAGE, exception.getMessage());
        verify(productRepository, times(1)).deleteById(dependentId);
    }

}
