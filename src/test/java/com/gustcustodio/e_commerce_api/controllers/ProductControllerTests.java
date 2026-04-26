package com.gustcustodio.e_commerce_api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gustcustodio.e_commerce_api.constructors.Factory;
import com.gustcustodio.e_commerce_api.dtos.CategoryResponseDTO;
import com.gustcustodio.e_commerce_api.dtos.ProductRequestDTO;
import com.gustcustodio.e_commerce_api.dtos.ProductResponseDTO;
import com.gustcustodio.e_commerce_api.services.ProductService;
import com.gustcustodio.e_commerce_api.services.exceptions.DatabaseException;
import com.gustcustodio.e_commerce_api.services.exceptions.ResourceNotFoundException;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ProductController.class)
public class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private Long existingId;
    private Long nonExistingId;
    private Long dependentId;
    private ProductRequestDTO productRequestDTO;
    private ProductResponseDTO productResponseDTO;
    private PageImpl<ProductResponseDTO> page;

    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 1000L;
        dependentId = 2L;
        productRequestDTO = Factory.createProductRequestDTO();
        productResponseDTO = Factory.createProductResponseDTO();
        page = new PageImpl<>(List.of(productResponseDTO));

        when(productService.findProductWithCategories(existingId)).thenReturn(productResponseDTO);
        when(productService.findProductWithCategories(nonExistingId)).thenThrow(ResourceNotFoundException.class);
        when(productService.findAllProducts(0, 5)).thenReturn(page);
        when(productService.createProduct(any(ProductRequestDTO.class))).thenReturn(productResponseDTO);
        when(productService.updateProduct(existingId, productRequestDTO)).thenReturn(productResponseDTO);
        when(productService.updateProduct(nonExistingId, productRequestDTO)).thenThrow(ResourceNotFoundException.class);
        doNothing().when(productService).deleteProduct(existingId);
        doThrow(ResourceNotFoundException.class).when(productService).deleteProduct(nonExistingId);
        doThrow(DatabaseException.class).when(productService).deleteProduct(dependentId);
    }

    @Test
    public void findProductWithCategoriesShouldReturnProductDTOWhenIdExists() throws Exception {
        ResultActions result = mockMvc.perform(get("/products/{id}", existingId).accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.name", CoreMatchers.is(productResponseDTO.name())));
    }

    @Test
    public void findProductWithCategoriesShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(get("/products/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void findAllProductsShouldReturnPage() throws Exception {
        ResultActions result = mockMvc.perform(get("/products")
                .param("pageNumber", "0")
                .param("pageSize", "5")
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(page.getContent().size())))
                .andExpect(jsonPath("$.content[0].name", is(productResponseDTO.name())));
    }

    @Test
    public void createProductShouldReturnProductResponseDTO() throws Exception {
        ResultActions result = mockMvc.perform(post("/products")
                .content(objectMapper.writeValueAsString(productRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.name", is(productResponseDTO.name())));
    }

    @ParameterizedTest
    @MethodSource(value = "providerOfInvalidProductRequestDTO")
    public void createProductShouldReturnUnprocessableEntityWhenProductRequestDTOHasInvalidData(
            String name,
            String description,
            Double price,
            Integer quantity,
            Set<CategoryResponseDTO> categories
    ) throws Exception {
        ProductRequestDTO invalidDTO = new ProductRequestDTO(name, description, price, quantity, categories);
        ResultActions result = mockMvc.perform(post("/products")
                .content(objectMapper.writeValueAsString(invalidDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void updateProductShouldReturnProductResponseDTOWhenIdExists() throws Exception {
        ResultActions result = mockMvc.perform(put("/products/{id}", existingId)
                .content(objectMapper.writeValueAsString(productRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.name", is(productRequestDTO.name())));
    }

    @Test
    public void updateShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(put("/products/{id}", nonExistingId)
                .content(objectMapper.writeValueAsString(productRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @MethodSource(value = "providerOfInvalidProductRequestDTO")
    public void updateProductShouldReturnUnprocessableEntityWhenProductRequestDTOHasInvalidData(
            String name,
            String description,
            Double price,
            Integer quantity,
            Set<CategoryResponseDTO> categories
    ) throws Exception {
        ProductRequestDTO invalidDTO = new ProductRequestDTO(name, description, price, quantity, categories);
        ResultActions result = mockMvc.perform(put("/products/{id}", existingId)
                .content(objectMapper.writeValueAsString(invalidDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void deleteShouldReturnNoContentWhenIdExists() throws Exception {
        ResultActions result = mockMvc.perform(delete("/products/{id}", existingId));
        result.andExpect(status().isNoContent());
    }

    @Test
    public void deleteShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(delete("/products/{id}", nonExistingId));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void deleteShouldReturnDatabaseExceptionWhenDependentId() throws Exception {
        ResultActions result = mockMvc.perform(delete("/products/{id}", dependentId));
        result.andExpect(status().isBadRequest());
    }

    static Stream<Arguments> providerOfInvalidProductRequestDTO() {
        return Stream.of(
                Arguments.of(null, "Valid Product Description", 50.0, 10, Set.of(Factory.createCategoryResponseDTO())),
                Arguments.of("Valid Product", null, 50.0, 10, Set.of(Factory.createCategoryResponseDTO())),
                Arguments.of("Valid Product", "Valid Product Description", -50.0, 10, Set.of(Factory.createCategoryResponseDTO())),
                Arguments.of("Valid Product", "Valid Product Description", 50.0, -10, Set.of(Factory.createCategoryResponseDTO()))
        );
    }

}
