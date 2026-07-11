package com.gustcustodio.e_commerce_api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gustcustodio.e_commerce_api.dtos.OrderItemDTO;
import com.gustcustodio.e_commerce_api.dtos.OrderRequestDTO;
import com.gustcustodio.e_commerce_api.factories.OrderFactory;
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenUtil tokenUtil;

    private Long existingId, nonExistingId, anotherId;
    private OrderRequestDTO orderRequestDTO;
    private String adminUsername, clientUsername;
    private String adminPassword, clientPassword;
    private String adminToken, clientToken, invalidToken;

    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        anotherId = 2L;
        nonExistingId = 1000L;
        OrderItemDTO orderItemDTO = new OrderItemDTO(1L, null, null, 2, null);
        orderRequestDTO = new OrderRequestDTO(List.of(orderItemDTO));
        adminUsername = "dickens@email.com";
        adminPassword = "12345678";
        clientUsername = "tolstoi@email.com";
        clientPassword = "12345678";
        adminToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername, adminPassword);
        clientToken = tokenUtil.obtainAccessToken(mockMvc, clientUsername, clientPassword);
        invalidToken = adminToken + "123";
    }

    @Test
    public void findOrderByIdShouldReturnOrderResponseDTOWhenLoggedAsAdminAndIdExists() throws Exception {
        ResultActions result = mockMvc.perform(get("/orders/{id}", existingId)
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId));
    }

    @Test
    public void findOrderByIdShouldReturnOrderResponseDTOWhenLoggedAsClientAndIdExistsAndPertencesToHim() throws Exception {
        ResultActions result = mockMvc.perform(get("/orders/{id}", existingId)
                .header("Authorization", "Bearer " + clientToken)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId));
    }

    @Test
    public void findOrderByIdShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        ResultActions result = mockMvc.perform(get("/orders/{id}", existingId)
                .header("Authorization", "Bearer " + invalidToken)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void findOrderByIdShouldReturnUnauthorizedWhenNotLogged() throws Exception {
        ResultActions result = mockMvc.perform(get("/orders/{id}", existingId)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void findOrderByIdShouldReturnForbiddenWhenOrderIdIsOfAnotherClient() throws Exception {
        ResultActions result = mockMvc.perform(get("/orders/{id}", anotherId)
                .header("Authorization", "Bearer " + clientToken)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isForbidden());
    }

    @Test
    public void findOrderByIdShouldReturnNotFoundWhenLoggedAsAdminAndIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(get("/orders/{id}", nonExistingId)
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void findOrderByIdShouldReturnNotFoundWhenLoggedAsClientAndIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(get("/orders/{id}", nonExistingId)
                .header("Authorization", "Bearer " + clientToken)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void findAllOrdersShouldReturnPageWhenLoggedAsAdmin() throws Exception {
        ResultActions result = mockMvc.perform(get("/orders")
                .header("Authorization", "Bearer " + adminToken)
                .param("pageNumber", "0")
                .param("pageSize", "5")
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content[0].items[0].name", is("Crime and Punishment")));
    }

    @Test
    public void findAllOrdersShouldReturnPageWhenLoggedAsClient() throws Exception {
        ResultActions result = mockMvc.perform(get("/orders")
                .header("Authorization", "Bearer " + clientToken)
                .param("pageNumber", "0")
                .param("pageSize", "5")
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content[0].items[0].name", is("1984")))
                .andExpect(jsonPath("$.content[0].items[1].name", is("Moby Dick")))
                .andExpect(jsonPath("$.content[1].items[0].name", is("The Great Gatsby")));
    }

    @Test
    public void findAllOrdersShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        ResultActions result = mockMvc.perform(get("/orders")
                .header("Authorization", "Bearer " + invalidToken)
                .param("pageNumber", "0")
                .param("pageSize", "5")
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void findAllOrdersShouldReturnUnauthorizedWhenNotLogged() throws Exception {
        ResultActions result = mockMvc.perform(get("/orders")
                .param("pageNumber", "0")
                .param("pageSize", "5")
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void createOrderShouldReturnOrderResponseDTOWhenLoggedAsAdminAndDataIsValid() throws Exception {
        ResultActions result = mockMvc.perform(post("/orders")
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(orderRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].name", is("1984")));
    }

    @Test
    public void createOrderShouldReturnOrderResponseDTOWhenLoggedAsClientAndDataIsValid() throws Exception {
        ResultActions result = mockMvc.perform(post("/orders")
                .header("Authorization", "Bearer " + clientToken)
                .content(objectMapper.writeValueAsString(orderRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].name", is("1984")));
    }

    @Test
    public void createOrderShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        ResultActions result = mockMvc.perform(post("/orders")
                .header("Authorization", "Bearer " + invalidToken)
                .content(objectMapper.writeValueAsString(orderRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void createOrderShouldReturnUnauthorizedWhenNotLogged() throws Exception {
        ResultActions result = mockMvc.perform(post("/orders")
                .content(objectMapper.writeValueAsString(orderRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void createOrderShouldReturnUnprocessableEntityWhenItemsListIsEmpty() throws Exception {
        OrderRequestDTO emptyOrderRequestDTO = new OrderRequestDTO(Collections.emptyList());
        ResultActions result = mockMvc.perform(post("/orders")
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(emptyOrderRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnprocessableEntity());
    }

    @ParameterizedTest
    @MethodSource(value = "providerOfInvalidOrderRequestDTO")
    public void createOrderShouldReturnUnprocessableEntityWhenOrderRequestDTOHasInvalidData(
            Long productId,
            String name,
            Double price,
            Integer quantity,
            Double subTotal
    ) throws Exception {
        OrderItemDTO invalidOrderItemDTO = new OrderItemDTO(productId, name, price, quantity, subTotal);
        OrderRequestDTO invalidOrderRequestDTO = new OrderRequestDTO(List.of(invalidOrderItemDTO));
        ResultActions result = mockMvc.perform(post("/orders")
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(invalidOrderRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void updateOrderShouldReturnOrderResponseDTOWhenLoggedAsAdminAndIdExists() throws Exception {
        ResultActions result = mockMvc.perform(put("/orders/{id}", existingId)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(orderRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name", is("1984")));
    }

    @Test
    public void updateOrderShouldReturnOrderResponseDTOWhenLoggedAsClientAndIdExistsAndPertencesToHim() throws Exception {
        ResultActions result = mockMvc.perform(put("/orders/{id}", existingId)
                .header("Authorization", "Bearer " + clientToken)
                .content(objectMapper.writeValueAsString(orderRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name", is("1984")));
    }

    @Test
    public void updateOrderShouldReturnNotFoundWhenLoggedAsAdminAndIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(put("/orders/{id}", nonExistingId)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(orderRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void updateOrderShouldReturnNotFoundWhenLoggedAsClientAndIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(put("/orders/{id}", nonExistingId)
                .header("Authorization", "Bearer " + clientToken)
                .content(objectMapper.writeValueAsString(orderRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void updateOrderShouldReturnForbiddenWhenLoggedAsClientAndOrderIdIsOfAnotherClient() throws Exception {
        ResultActions result = mockMvc.perform(put("/orders/{id}", anotherId)
                .header("Authorization", "Bearer " + clientToken)
                .content(objectMapper.writeValueAsString(orderRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isForbidden());
    }

    @Test
    public void updateOrderShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        ResultActions result = mockMvc.perform(put("/orders/{id}", existingId)
                .header("Authorization", "Bearer " + invalidToken)
                .content(objectMapper.writeValueAsString(orderRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void updateOrderShouldReturnUnauthorizedWhenNotLogged() throws Exception {
        ResultActions result = mockMvc.perform(put("/orders/{id}", existingId)
                .content(objectMapper.writeValueAsString(orderRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource(value = "providerOfInvalidOrderRequestDTO")
    public void updateOrderShouldReturnUnprocessableEntityWhenOrderRequestDTOHasInvalidData(
            Long productId,
            String name,
            Double price,
            Integer quantity,
            Double subTotal
    ) throws Exception {
        OrderItemDTO invalidOrderItemDTO = new OrderItemDTO(productId, name, price, quantity, subTotal);
        OrderRequestDTO invalidOrderRequestDTO = new OrderRequestDTO(List.of(invalidOrderItemDTO));
        ResultActions result = mockMvc.perform(put("/orders/{id}", existingId)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(invalidOrderRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void deleteOrderShouldReturnNoContentWhenLoggedAsAdminAndIdExists() throws Exception {
        ResultActions result = mockMvc.perform(delete("/orders/{id}", existingId)
                .header("Authorization", "Bearer " + adminToken));
        result.andExpect(status().isNoContent());
    }

    @Test
    public void deleteOrderShouldReturnNoContentWhenLoggedAsClientAndIdExistsAndPertencesToHim() throws Exception {
        ResultActions result = mockMvc.perform(delete("/orders/{id}", existingId)
                .header("Authorization", "Bearer " + clientToken));
        result.andExpect(status().isNoContent());
    }

    @Test
    public void deleteOrderShouldReturnNotFoundWhenLoggedAsAdminAndIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(delete("/orders/{id}", nonExistingId)
                .header("Authorization", "Bearer " + adminToken));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void deleteOrderShouldReturnNotFoundWhenLoggedAsClientAndIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(delete("/orders/{id}", nonExistingId)
                .header("Authorization", "Bearer " + clientToken));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void deleteOrderShouldReturnUnauthorizedWhenNotLogged() throws Exception {
        ResultActions result = mockMvc.perform(delete("/orders/{id}", existingId));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteOrderShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        ResultActions result = mockMvc.perform(delete("/orders/{id}", existingId)
                .header("Authorization", "Bearer " + invalidToken));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteOrderShouldReturnForbiddenWhenLoggedAsClientAndOrderIdIsOfAnotherClient() throws Exception {
        ResultActions result = mockMvc.perform(delete("/orders/{id}", anotherId)
                .header("Authorization", "Bearer " + clientToken));
        result.andExpect(status().isForbidden());
    }

    static Stream<Arguments> providerOfInvalidOrderRequestDTO() {
        return Stream.of(
                Arguments.of(0L, "Test", 10.0, 1, 10.0),
                Arguments.of(-1L, "Test", 10.0, 1, 10.0),
                Arguments.of(1L, "Test", 10.0, 0, 10.0),
                Arguments.of(1L, "Test", 10.0, -1, 10.0)
        );
    }

}
