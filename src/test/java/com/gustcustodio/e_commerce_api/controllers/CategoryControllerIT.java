package com.gustcustodio.e_commerce_api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gustcustodio.e_commerce_api.dtos.CategoryRequestDTO;
import com.gustcustodio.e_commerce_api.factories.CategoryFactory;
import com.gustcustodio.e_commerce_api.utils.TokenUtil;
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

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CategoryControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenUtil tokenUtil;

    private Long existingId, nonExistingId;
    private CategoryRequestDTO categoryRequestDTO;
    private String adminUsername, clientUsername;
    private String adminPassword, clientPassword;
    private String adminToken, clientToken, invalidToken;

    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 1000L;
        categoryRequestDTO = CategoryFactory.createCategoryRequestDTO();
        adminUsername = "dickens@email.com";
        adminPassword = "12345678";
        clientUsername = "tolstoi@email.com";
        clientPassword = "12345678";
        adminToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername, adminPassword);
        clientToken = tokenUtil.obtainAccessToken(mockMvc, clientUsername, clientPassword);
        invalidToken = adminToken + "123";
    }

    @Test
    public void findCategoryByIdShouldReturnCategoryResponseDTOWhenIdExists() throws Exception {
        ResultActions result = mockMvc.perform(get("/categories/{id}", existingId).accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.name").value("Books"));
    }

    @Test
    public void findCategoryByIdShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(get("/categories/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void findAllCategoriesShouldReturnPageOfCategoryResponseDTO() throws Exception {
        ResultActions result = mockMvc.perform(get("/categories")
                .param("pageNumber", "0")
                .param("pageSize", "5")
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content[0].name").value("Books"))
                .andExpect(jsonPath("$.content[1].name").value("Electronics"))
                .andExpect(jsonPath("$.content[2].name").value("Home Office"));
    }

    @Test
    public void createCategoryShouldReturnCategoryResponseDTOWhenLoggedAsAdminAndDataIsValid() throws Exception {
        ResultActions result = mockMvc.perform(post("/categories")
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(categoryRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(categoryRequestDTO.name())));
    }

    @Test
    public void createCategoryShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        ResultActions result = mockMvc.perform(post("/categories")
                .header("Authorization", "Bearer " + invalidToken)
                .content(objectMapper.writeValueAsString(categoryRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void createCategoryShouldReturnUnauthorizedWhenNotLogged() throws Exception {
        ResultActions result = mockMvc.perform(post("/categories")
                .content(objectMapper.writeValueAsString(categoryRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void createCategoryShouldReturnForbiddenWhenLoggedAsClient() throws Exception {
        ResultActions result = mockMvc.perform(post("/categories")
                .header("Authorization", "Bearer " + clientToken)
                .content(objectMapper.writeValueAsString(categoryRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @MethodSource(value = "providerOfInvalidCategoryRequestDTO")
    public void createCategoryShouldReturnUnprocessableEntityWhenLoggedAsAdminAndDataIsInvalid(
            String name
    ) throws Exception {
        CategoryRequestDTO invalidDTO = new CategoryRequestDTO(name);
        ResultActions result = mockMvc.perform(post("/categories")
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(invalidDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void updateCategoryShouldReturnCategoryResponseDTOWhenLoggedAsAdminAndDataIsValid() throws Exception {
        ResultActions result = mockMvc.perform(put("/categories/{id}", existingId)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(categoryRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.name", is(categoryRequestDTO.name())));
    }

    @Test
    public void updateCategoryShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        ResultActions result = mockMvc.perform(put("/categories/{id}", existingId)
                .header("Authorization", "Bearer " + invalidToken)
                .content(objectMapper.writeValueAsString(categoryRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void updateCategoryShouldReturnUnauthorizedWhenNotLogged() throws Exception {
        ResultActions result = mockMvc.perform(put("/categories/{id}", existingId)
                .content(objectMapper.writeValueAsString(categoryRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void updateCategoryShouldReturnForbiddenWhenLoggedAsClient() throws Exception {
        ResultActions result = mockMvc.perform(put("/categories/{id}", existingId)
                .header("Authorization", "Bearer " + clientToken)
                .content(objectMapper.writeValueAsString(categoryRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isForbidden());
    }

    @Test
    public void updateCategoryShouldReturnNotFoundWhenLoggedAsAdminIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(put("/categories/{id}", nonExistingId)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(categoryRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @MethodSource(value = "providerOfInvalidCategoryRequestDTO")
    public void updateCategoryShouldReturnUnprocessableEntityWhenLoggedAsAdminAndDataIsInvalid(
            String name
    ) throws Exception {
        CategoryRequestDTO invalidDTO = new CategoryRequestDTO(name);
        ResultActions result = mockMvc.perform(put("/categories/{id}", existingId)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(invalidDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void deleteCategoryShouldReturnNoContentWhenLoggedAsAdminAndIdExists() throws Exception {
        ResultActions result = mockMvc.perform(delete("/categories/{id}", existingId)
                .header("Authorization", "Bearer " + adminToken));
        result.andExpect(status().isNoContent());
    }

    @Test
    public void deleteCategoryShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        ResultActions result = mockMvc.perform(delete("/categories/{id}", existingId)
                .header("Authorization", "Bearer " + invalidToken));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteCategoryShouldReturnForbiddenWhenLoggedAsClient() throws Exception {
        ResultActions result = mockMvc.perform(delete("/categories/{id}", existingId)
                .header("Authorization", "Bearer " + clientToken));
        result.andExpect(status().isForbidden());
    }

    @Test
    public void deleteCategoryShouldReturnNotFoundWhenLoggedAsAdminIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(delete("/categories/{id}", nonExistingId)
                .header("Authorization", "Bearer " + adminToken));
        result.andExpect(status().isNotFound());
    }

    static Stream<Arguments> providerOfInvalidCategoryRequestDTO() {
        return Stream.of(
                Arguments.of((String) null),
                Arguments.of(""),
                Arguments.of("A"),
                Arguments.of("A".repeat(51))
        );
    }

}
