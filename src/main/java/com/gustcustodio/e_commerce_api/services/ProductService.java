package com.gustcustodio.e_commerce_api.services;

import com.gustcustodio.e_commerce_api.dtos.ProductRequestDTO;
import com.gustcustodio.e_commerce_api.dtos.ProductResponseDTO;
import com.gustcustodio.e_commerce_api.entities.Product;
import com.gustcustodio.e_commerce_api.repositories.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findProductWithCategories(Long id) {
        Product entity = productRepository.findProductWithCategories(id).orElseThrow();
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

    public void convertDtoToEntity(ProductRequestDTO productRequestDTO, Product entity) {
        entity.setName(productRequestDTO.name());
        entity.setDescription(productRequestDTO.description());
        entity.setPrice(productRequestDTO.price());
        entity.setQuantity(productRequestDTO.quantity());
    }

}
