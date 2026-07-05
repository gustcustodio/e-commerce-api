package com.gustcustodio.e_commerce_api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gustcustodio.e_commerce_api.config.JwtAuthenticationFilter;
import com.gustcustodio.e_commerce_api.dtos.OrderItemDTO;
import com.gustcustodio.e_commerce_api.dtos.OrderRequestDTO;
import com.gustcustodio.e_commerce_api.dtos.OrderResponseDTO;
import com.gustcustodio.e_commerce_api.factories.OrderFactory;
import com.gustcustodio.e_commerce_api.services.OrderService;
import com.gustcustodio.e_commerce_api.services.exceptions.ResourceNotFoundException;
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private Long existingOrderId, nonExistingOrderId;
    private OrderItemDTO orderItemDTO;
    private OrderResponseDTO orderResponseDTO;
    private OrderRequestDTO orderRequestDTO;
    private PageImpl<OrderResponseDTO> page;

    @BeforeEach
    void setUp() throws Exception {
        existingOrderId = 1L;
        nonExistingOrderId = 1000L;
        orderItemDTO = new OrderItemDTO(1L, null, null, 2, null);
        orderResponseDTO = OrderFactory.createOrderResponseDTO();
        orderRequestDTO = new OrderRequestDTO(List.of(orderItemDTO));
        page = new PageImpl<>(List.of(orderResponseDTO));

        when(orderService.findOrderById(existingOrderId)).thenReturn(orderResponseDTO);
        when(orderService.findOrderById(nonExistingOrderId)).thenThrow(ResourceNotFoundException.class);
        when(orderService.findAllOrders(0, 5)).thenReturn(page);
        when(orderService.createOrder(any(OrderRequestDTO.class))).thenReturn(orderResponseDTO);
        when(orderService.updateOrder(eq(existingOrderId), any(OrderRequestDTO.class))).thenReturn(orderResponseDTO);
        when(orderService.updateOrder(eq(nonExistingOrderId), any(OrderRequestDTO.class))).thenThrow(ResourceNotFoundException.class);
        doNothing().when(orderService).deleteOrder(existingOrderId);
        doThrow(ResourceNotFoundException.class).when(orderService).deleteOrder(nonExistingOrderId);
    }

    @Test
    public void findOrderByIdShouldReturnOrderResponseDTOWhenIdExists() throws Exception {
        ResultActions result = mockMvc.perform(get("/orders/{id}", existingOrderId).accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingOrderId));
    }

    @Test
    public void findOrderByIdShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(get("/orders/{id}", nonExistingOrderId).accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void findAllOrdersShouldReturnPage() throws Exception {
        ResultActions result = mockMvc.perform(get("/orders")
                .param("pageNumber", "0")
                .param("pageSize", "5")
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(page.getContent().size())))
                .andExpect(jsonPath("$.content[0].id").value(existingOrderId));
    }

    @Test
    public void createOrderShouldReturnOrderResponseDTO() throws Exception {
        ResultActions result = mockMvc.perform(post("/orders")
                .content(objectMapper.writeValueAsString(orderRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(existingOrderId));
    }

    @Test
    public void createOrderShouldReturnUnprocessableEntityWhenItemsListIsEmpty() throws Exception {
        OrderRequestDTO emptyOrderRequestDTO = new OrderRequestDTO(Collections.emptyList());
        ResultActions result = mockMvc.perform(post("/orders")
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
                .content(objectMapper.writeValueAsString(invalidOrderRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andDo(print());
        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void updateOrderShouldReturnOrderResponseDTOWhenIdExists() throws Exception {
        ResultActions result = mockMvc.perform(put("/orders/{id}", existingOrderId)
                .content(objectMapper.writeValueAsString(orderRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingOrderId))
                .andExpect(jsonPath("$.orderStatus", is(orderResponseDTO.orderStatus().toString())));
    }

    @Test
    public void updateOrderShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(put("/orders/{id}", nonExistingOrderId)
                .content(objectMapper.writeValueAsString(orderRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
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
        ResultActions result = mockMvc.perform(put("/orders/{id}", existingOrderId)
                .content(objectMapper.writeValueAsString(invalidOrderRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void deleteOrderShouldReturnNoContentWhenIdExists() throws Exception {
        ResultActions result = mockMvc.perform(delete("/orders/{id}", existingOrderId));
        result.andExpect(status().isNoContent());
    }

    @Test
    public void deleteOrderShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(delete("/orders/{id}", nonExistingOrderId));
        result.andExpect(status().isNotFound());
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
