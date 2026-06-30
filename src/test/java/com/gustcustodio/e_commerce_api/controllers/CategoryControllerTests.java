package com.gustcustodio.e_commerce_api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gustcustodio.e_commerce_api.config.JwtAuthenticationFilter;
import com.gustcustodio.e_commerce_api.factories.CategoryFactory;
import com.gustcustodio.e_commerce_api.factories.ProductFactory;
import com.gustcustodio.e_commerce_api.dtos.CategoryRequestDTO;
import com.gustcustodio.e_commerce_api.dtos.CategoryResponseDTO;
import com.gustcustodio.e_commerce_api.services.CategoryService;
import com.gustcustodio.e_commerce_api.services.UserService;
import com.gustcustodio.e_commerce_api.services.exceptions.DatabaseException;
import com.gustcustodio.e_commerce_api.services.exceptions.ResourceNotFoundException;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CategoryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private Long existingId;
    private Long nonExistingId;
    private Long dependentId;
    private CategoryRequestDTO categoryRequestDTO;
    private CategoryResponseDTO categoryResponseDTO;
    private PageImpl<CategoryResponseDTO> page;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 1000L;
        dependentId = 2L;
        categoryRequestDTO = CategoryFactory.createCategoryRequestDTO();
        categoryResponseDTO = CategoryFactory.createCategoryResponseDTO();
        page = new PageImpl<>(List.of(categoryResponseDTO));

        when(categoryService.findCategoryById(existingId)).thenReturn(categoryResponseDTO);
        when(categoryService.findCategoryById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
        when(categoryService.findAllCategories(0, 5)).thenReturn(page);
        when(categoryService.createCategory(any(CategoryRequestDTO.class))).thenReturn(categoryResponseDTO);
        when(categoryService.updateCategory(existingId, categoryRequestDTO)).thenReturn(categoryResponseDTO);
        when(categoryService.updateCategory(nonExistingId, categoryRequestDTO)).thenThrow(ResourceNotFoundException.class);
        doNothing().when(categoryService).deleteCategory(existingId);
        doThrow(ResourceNotFoundException.class).when(categoryService).deleteCategory(nonExistingId);
        doThrow(DatabaseException.class).when(categoryService).deleteCategory(dependentId);
    }

    @Test
    public void findCategoryByIdShouldReturnCategoryDTOWhenIdExists() throws Exception {
        ResultActions result = mockMvc.perform(get("/categories/{id}", existingId).accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.name", CoreMatchers.is(categoryResponseDTO.name())));
    }

    @Test
    public void findCategoryByIdShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(get("/categories/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void findAllCategoriesShouldReturnPage() throws Exception {
        ResultActions result = mockMvc.perform(get("/categories")
                .param("pageNumber", "0")
                .param("pageSize", "5")
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(page.getContent().size())))
                .andExpect(jsonPath("$.content[0].name", is(categoryResponseDTO.name())));
    }

    @Test
    public void createCategoryShouldReturnCategoryResponseDTO() throws Exception {
        ResultActions result = mockMvc.perform(post("/categories")
                .content(objectMapper.writeValueAsString(categoryRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.name", is(categoryResponseDTO.name())));
    }

    @ParameterizedTest
    @MethodSource(value = "providerOfInvalidCategoryRequestDTO")
    public void createCategoryShouldReturnUnprocessableEntityWhenCategoryRequestDTOHasInvalidData(
            String name
    ) throws Exception {
        CategoryRequestDTO invalidDTO = new CategoryRequestDTO(name);
        ResultActions result = mockMvc.perform(post("/categories")
                .content(objectMapper.writeValueAsString(invalidDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void updateCategoryShouldReturnCategoryResponseDTOWhenIdExists() throws Exception {
        ResultActions result = mockMvc.perform(put("/categories/{id}", existingId)
                .content(objectMapper.writeValueAsString(categoryRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.name", is(categoryRequestDTO.name())));
    }

    @Test
    public void updateShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(put("/categories/{id}", nonExistingId)
                .content(objectMapper.writeValueAsString(categoryRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @MethodSource(value = "providerOfInvalidCategoryRequestDTO")
    public void updateCategoryShouldReturnUnprocessableEntityWhenCategoryRequestDTOHasInvalidData(String name) throws Exception {
        CategoryRequestDTO invalidDTO = new CategoryRequestDTO(name);
        ResultActions result = mockMvc.perform(put("/categories/{id}", existingId)
                .content(objectMapper.writeValueAsString(invalidDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void deleteShouldReturnNoContentWhenIdExists() throws Exception {
        ResultActions result = mockMvc.perform(delete("/categories/{id}", existingId));
        result.andExpect(status().isNoContent());
    }

    @Test
    public void deleteShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(delete("/categories/{id}", nonExistingId));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void deleteShouldReturnDatabaseExceptionWhenDependentId() throws Exception {
        ResultActions result = mockMvc.perform(delete("/categories/{id}", dependentId));
        result.andExpect(status().isBadRequest());
    }

    static Stream<Arguments> providerOfInvalidCategoryRequestDTO() {
        return Stream.of(
                Arguments.of((String) null),
                Arguments.of(""),
                Arguments.of("   ")
        );
    }

}


