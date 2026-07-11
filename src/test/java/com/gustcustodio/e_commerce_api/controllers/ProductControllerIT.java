package com.gustcustodio.e_commerce_api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gustcustodio.e_commerce_api.dtos.CategoryResponseDTO;
import com.gustcustodio.e_commerce_api.factories.CategoryFactory;
import com.gustcustodio.e_commerce_api.utils.TokenUtil;
import com.gustcustodio.e_commerce_api.dtos.ProductRequestDTO;
import com.gustcustodio.e_commerce_api.factories.ProductFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenUtil tokenUtil;

    private Long existingId, nonExistingId;
    private ProductRequestDTO productRequestDTO;
    private String adminUsername, clientUsername;
    private String adminPassword, clientPassword;
    private String adminToken, clientToken, invalidToken;

    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 1000L;
        productRequestDTO = ProductFactory.createProductRequestDTO();
        adminUsername = "dickens@email.com";
        adminPassword = "12345678";
        clientUsername = "tolstoi@email.com";
        clientPassword = "12345678";
        adminToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername, adminPassword);
        clientToken = tokenUtil.obtainAccessToken(mockMvc, clientUsername, clientPassword);
        invalidToken = adminToken + "123";
    }

    @Test
    public void findProductWithCategoriesShouldReturnProductResponseDTOWhenIdExists() throws Exception {
        ResultActions result = mockMvc.perform(get("/products/{id}", existingId).accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.name").value("1984"))
                .andExpect(jsonPath("$.description").value("George Orwells chilling prophecy about the future."));
    }

    @Test
    public void findProductWithCategoriesShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(get("/products/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void findAllProductsShouldReturnPageOfProductResponseDTO() throws Exception {
        ResultActions result = mockMvc.perform(get("/products")
                .param("pageNumber", "0")
                .param("pageSize", "5")
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content[0].name").value(is("1984")))
                .andExpect(jsonPath("$.content[1].name").value(is("The Great Gatsby")))
                .andExpect(jsonPath("$.content[2].name").value(is("Frankenstein")));
    }

    @Test
    public void createProductShouldReturnProductResponseDTOWhenLoggedAsAdminAndDataIsValid() throws Exception {
        ResultActions result = mockMvc.perform(post("/products")
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(productRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(productRequestDTO.name())));
    }

    @Test
    public void createProductShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        ResultActions result = mockMvc.perform(post("/products")
                .header("Authorization", "Bearer " + invalidToken)
                .content(objectMapper.writeValueAsString(productRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void createProductShouldReturnUnauthorizedWhenNotLogged() throws Exception {
        ResultActions result = mockMvc.perform(post("/products")
                .content(objectMapper.writeValueAsString(productRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void createProductShouldReturnForbiddenWhenLoggedAsClient() throws Exception {
        ResultActions result = mockMvc.perform(post("/products")
                .header("Authorization", "Bearer " + clientToken)
                .content(objectMapper.writeValueAsString(productRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @MethodSource(value = "providerOfInvalidProductRequestDTO")
    public void createProductShouldReturnUnprocessableEntityLoggedAsAdminAndDataIsInvalid(
            String name,
            String description,
            Double price,
            Integer quantity,
            Set<CategoryResponseDTO> categories
    ) throws Exception {
        ProductRequestDTO invalidDTO = new ProductRequestDTO(name, description, price, quantity, categories);
        ResultActions result = mockMvc.perform(post("/products")
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(invalidDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void updateProductShouldReturnProductResponseDTOWhenLoggedAsAdminAndDataIsValid() throws Exception {
        ResultActions result = mockMvc.perform(put("/products/{id}", existingId)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(productRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.name", is(productRequestDTO.name())));
    }

    @Test
    public void updateProductShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        ResultActions result = mockMvc.perform(put("/products/{id}", existingId)
                .header("Authorization", "Bearer " + invalidToken)
                .content(objectMapper.writeValueAsString(productRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void updateProductShouldReturnUnauthorizedWhenNotLogged() throws Exception {
        ResultActions result = mockMvc.perform(put("/products/{id}", existingId)
                .content(objectMapper.writeValueAsString(productRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void updateProductShouldReturnForbiddenWhenLoggedAsClient() throws Exception {
        ResultActions result = mockMvc.perform(put("/products/{id}", existingId)
                .header("Authorization", "Bearer " + clientToken)
                .content(objectMapper.writeValueAsString(productRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isForbidden());
    }

    @Test
    public void updateProductShouldReturnNotFoundWhenLoggedAsAdminIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(put("/products/{id}", nonExistingId)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(productRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @MethodSource(value = "providerOfInvalidProductRequestDTO")
    public void updateProductShouldReturnUnprocessableEntityLoggedAsAdminAndDataIsInvalid(
            String name,
            String description,
            Double price,
            Integer quantity,
            Set<CategoryResponseDTO> categories
    ) throws Exception {
        ProductRequestDTO invalidDTO = new ProductRequestDTO(name, description, price, quantity, categories);
        ResultActions result = mockMvc.perform(put("/products/{id}", existingId)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(invalidDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnprocessableEntity());
    }


    @Test
    public void deleteProductShouldReturnNoContentWhenLoggedAsAdminAndIdExists() throws Exception {
        ResultActions result = mockMvc.perform(delete("/products/{id}", existingId)
                .header("Authorization", "Bearer " + adminToken));
        result.andExpect(status().isNoContent());
    }

    @Test
    public void deleteProductShouldReturnUnauthorizedWhenNotLogged() throws Exception {
        ResultActions result = mockMvc.perform(delete("/products/{id}", existingId));
        result.andExpect(status().isUnauthorized());
    }
    @Test
    public void deleteProductShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        ResultActions result = mockMvc.perform(delete("/products/{id}", existingId)
                .header("Authorization", "Bearer " + invalidToken));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteProductShouldReturnForbiddenWhenLoggedAsClient() throws Exception {
        ResultActions result = mockMvc.perform(delete("/products/{id}", existingId)
                .header("Authorization", "Bearer " + clientToken));
        result.andExpect(status().isForbidden());
    }

    @Test
    public void deleteProductShouldReturnNotFoundWhenLoggedAsAdminIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(delete("/products/{id}", nonExistingId)
                .header("Authorization", "Bearer " + adminToken));
        result.andExpect(status().isNotFound());
    }

    static Stream<Arguments> providerOfInvalidProductRequestDTO() {
        return Stream.of(
                Arguments.of(null, "Invalid Product Description", 50.0, 10, Set.of(CategoryFactory.createCategoryResponseDTO())),
                Arguments.of("Invalid Product", null, 50.0, 10, Set.of(CategoryFactory.createCategoryResponseDTO())),
                Arguments.of("Invalid Product", "Invalid Product Description", -50.0, 10, Set.of(CategoryFactory.createCategoryResponseDTO())),
                Arguments.of("Invalid Product", "Invalid Product Description", 50.0, -10, Set.of(CategoryFactory.createCategoryResponseDTO())),
                Arguments.of("Invalid Product", "Invalid Product Description", 50.0, 10, Set.of())
        );
    }

}