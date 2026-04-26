package com.gustcustodio.e_commerce_api.services;

import com.gustcustodio.e_commerce_api.dtos.CategoryResponseDTO;
import com.gustcustodio.e_commerce_api.dtos.ProductRequestDTO;
import com.gustcustodio.e_commerce_api.dtos.ProductResponseDTO;
import com.gustcustodio.e_commerce_api.entities.Category;
import com.gustcustodio.e_commerce_api.entities.Product;
import com.gustcustodio.e_commerce_api.repositories.CategoryRepository;
import com.gustcustodio.e_commerce_api.repositories.ProductRepository;
import com.gustcustodio.e_commerce_api.services.exceptions.DatabaseException;
import com.gustcustodio.e_commerce_api.services.exceptions.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findProductWithCategories(Long id) {
        Product entity = productRepository.findProductWithCategories(id).orElseThrow(ResourceNotFoundException::new);
        return new ProductResponseDTO(entity);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findAllProducts(Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        Page<Product> page = productRepository.findAllProducts(pageRequest);
        return page.map(ProductResponseDTO::new);
    }

    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {
        Product entity = new Product();
        convertDtoToEntity(productRequestDTO, entity);
        entity = productRepository.save(entity);
        return new ProductResponseDTO(entity);
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequestDTO) {
        Product entity = productRepository.findProductWithCategories(id).orElseThrow(ResourceNotFoundException::new);
        convertDtoToEntity(productRequestDTO, entity);
        entity = productRepository.save(entity);
        return new ProductResponseDTO(entity);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) throw new ResourceNotFoundException();
        try {
            productRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException();
        }
    }

    public void convertDtoToEntity(ProductRequestDTO productRequestDTO, Product entity) {
        entity.setName(productRequestDTO.name());
        entity.setDescription(productRequestDTO.description());
        entity.setPrice(productRequestDTO.price());
        entity.setQuantity(productRequestDTO.quantity());
        entity.getCategories().clear();
        for (CategoryResponseDTO categoryResponseDTO : productRequestDTO.categories()) {
            Category category = categoryRepository.getReferenceById(categoryResponseDTO.id());
            entity.getCategories().add(category);
        }
    }

}
