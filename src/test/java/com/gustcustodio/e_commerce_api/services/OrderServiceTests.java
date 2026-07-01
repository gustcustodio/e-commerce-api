package com.gustcustodio.e_commerce_api.services;

import com.gustcustodio.e_commerce_api.dtos.OrderItemDTO;
import com.gustcustodio.e_commerce_api.dtos.OrderRequestDTO;
import com.gustcustodio.e_commerce_api.dtos.OrderResponseDTO;
import com.gustcustodio.e_commerce_api.entities.*;
import com.gustcustodio.e_commerce_api.factories.ProductFactory;
import com.gustcustodio.e_commerce_api.factories.OrderFactory;
import com.gustcustodio.e_commerce_api.factories.UserFactory;
import com.gustcustodio.e_commerce_api.repositories.OrderItemRepository;
import com.gustcustodio.e_commerce_api.repositories.OrderRepository;
import com.gustcustodio.e_commerce_api.repositories.ProductRepository;
import com.gustcustodio.e_commerce_api.services.exceptions.ForbiddenException;
import com.gustcustodio.e_commerce_api.services.exceptions.OrderStatusException;
import com.gustcustodio.e_commerce_api.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class OrderServiceTests {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private ProductRepository productRepository;

    private static final String RESOURCE_NOT_FOUND_EXCEPTION_MESSAGE = "Resource not found";
    private static final String FORBIDDEN_EXCEPTION_MESSAGE = "Access denied";
    private static final String ORDER_STATUS_EXCEPTION_MESSAGE = "Order can only be updated while awaiting payment";

    private Long existingId, nonExistingId;
    private Order validOrder;
    private OrderItemDTO orderItemDTO;
    private OrderRequestDTO orderRequestDTO;
    private Product validProduct, invalidProduct;
    private User userAdminAndClient;
    private Integer pageNumber, pageSize;
    private PageRequest pageRequest;
    private List<Long> ordersId;
    private Page<Long> ordersIdPage;
    private Page<Long> emptyIdsPage;
    private List<Order> orders;

    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 1000L;
        validOrder = OrderFactory.createValidOrder();
        orderItemDTO = new OrderItemDTO(1L, null, null, 2, null);
        orderRequestDTO = new OrderRequestDTO(List.of(orderItemDTO));
        validProduct = ProductFactory.createValidProduct();
        invalidProduct = ProductFactory.createInvalidProduct();
        userAdminAndClient = UserFactory.createUserAdminAndClient();
        pageNumber = 0;
        pageSize = 5;
        pageRequest = PageRequest.of(pageNumber, pageSize);
        ordersId = List.of(1L, 2L);
        ordersIdPage = new PageImpl<>(ordersId, pageRequest, 2L);
        emptyIdsPage = new PageImpl<>(List.of(), pageRequest, 0L);
        orders = List.of(OrderFactory.createValidOrder(), OrderFactory.createValidOrder());

        doNothing().when(authenticationService).validateUser(any());
        doNothing().when(orderRepository).deleteOrderById(existingId);
        doNothing().when(orderItemRepository).deleteByOrderId(existingId);

        when(authenticationService.loggedUser()).thenReturn(userAdminAndClient);
        when(orderRepository.findById(existingId)).thenReturn(Optional.of(validOrder));
        when(orderRepository.findById(nonExistingId)).thenReturn(Optional.empty());
        when(orderRepository.findOrderById(existingId)).thenReturn(Optional.of(validOrder));
        when(orderRepository.findOrderById(nonExistingId)).thenReturn(Optional.empty());
        when(productRepository.getReferenceById(existingId)).thenReturn(validProduct);
        when(productRepository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);
    }

    @Test
    public void findOrderByIdShouldReturnOrderResponseDTOWhenIdExistsAndAdminOrSelfClientLogged() {
        OrderResponseDTO result = orderService.findOrderById(existingId);
        Assertions.assertNotNull(result);
    }

    @Test
    public void findByIdShouldThrowsForbiddenExceptionWhenIdExistsAndOtherClientLogged() {
        doThrow(new ForbiddenException()).when(authenticationService).validateUser(any());
        ForbiddenException exception =
                Assertions.assertThrows(ForbiddenException.class, () -> orderService.findOrderById(existingId));
        Assertions.assertEquals(FORBIDDEN_EXCEPTION_MESSAGE, exception.getMessage());
    }

    @Test
    public void findOrderByIdShouldThrowsResourceNotFoundExceptionWhenIdDoesNotExistsAndAdminOrSelfClientLogged() {
        ResourceNotFoundException exception =
                Assertions.assertThrows(ResourceNotFoundException.class, () -> orderService.findOrderById(nonExistingId));
        Assertions.assertEquals(RESOURCE_NOT_FOUND_EXCEPTION_MESSAGE, exception.getMessage());
    }

    @Test
    public void findAllOrdersShouldReturnPagedOrderResponseDTOWhenAndAdminOrSelfClientLogged() {
        when(orderRepository.findOrderIdsByClientId(userAdminAndClient.getId(), pageRequest)).thenReturn(ordersIdPage);
        when(orderRepository.findOrdersWithDetailsByIds(ordersId)).thenReturn(orders);
        Page<OrderResponseDTO> result = orderService.findAllOrders(pageNumber, pageSize);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.getContent().size());
        verify(authenticationService, times(1)).loggedUser();
        verify(orderRepository, times(1)).findOrderIdsByClientId(userAdminAndClient.getId(), pageRequest);
        verify(orderRepository, times(1)).findOrdersWithDetailsByIds(ordersId);
    }

    @Test
    public void findAllOrdersShouldReturnEmptyPageWhenNoOrdersExist() {
        when(orderRepository.findOrderIdsByClientId(userAdminAndClient.getId(), pageRequest)).thenReturn(emptyIdsPage);
        Page<OrderResponseDTO> result = orderService.findAllOrders(pageNumber, pageSize);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertEquals(0L, result.getTotalElements());
        verify(orderRepository, never()).findOrdersWithDetailsByIds(ordersId);
    }

    @Test
    public void createOrderShouldReturnOrderResponseDTOWhenAdminOrClientLogged() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        OrderResponseDTO result = orderService.createOrder(orderRequestDTO);
        Assertions.assertNotNull(result);
        verify(authenticationService, times(1)).loggedUser();
        verify(productRepository, times(1)).getReferenceById(existingId);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderItemRepository, times(1)).saveAll(any());
    }

    @Test
    public void createOrderShouldThrowsResourceNotFoundExceptionWhenOrderProductIdDoesNotExist() {
        OrderItem orderItem = new OrderItem(validOrder, invalidProduct, 2, 10.0);
        validOrder.getOrderItems().add(orderItem);
        orderRequestDTO = new OrderRequestDTO(List.of(new OrderItemDTO(orderItem)));
        ResourceNotFoundException exception =
                Assertions.assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(orderRequestDTO));
        Assertions.assertEquals(RESOURCE_NOT_FOUND_EXCEPTION_MESSAGE, exception.getMessage());
    }

    @Test
    public void updateOrderShouldReturnOrderResponseDTOWhenIdExistsAndAdminOrSelfClientLogged() {
        OrderResponseDTO result = orderService.updateOrder(existingId, orderRequestDTO);
        Assertions.assertNotNull(result);
        verify(authenticationService, times(1)).validateUser(any());
    }

    @Test
    public void updateOrderShouldUpdateQuantityWhenItemAlreadyExistsInOrderAndAdminOrSelfClientLogged() {
        validOrder.getOrderItems().clear();
        OrderItem orderItem = new OrderItem(validOrder, validProduct, 2, 10.0);
        validOrder.getOrderItems().add(orderItem);
        orderItemDTO = new OrderItemDTO(orderItem.getProduct().getId(), null, null, 5, null);
        orderRequestDTO = new OrderRequestDTO(List.of(orderItemDTO));
        OrderResponseDTO result = orderService.updateOrder(existingId, orderRequestDTO);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(5, orderItem.getQuantity());
        verify(productRepository, never()).getReferenceById(any());
    }

    @Test
    public void updateOrderShouldAddNewItemWhenItemDoesNotExistInOrderAndAdminOrSelfClientLogged() {
        validOrder.getOrderItems().clear();
        OrderItem orderItem = new OrderItem(validOrder, validProduct, 2, 10.0);
        orderRequestDTO = new OrderRequestDTO(List.of(new OrderItemDTO(orderItem)));
        OrderResponseDTO result = orderService.updateOrder(existingId, orderRequestDTO);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, validOrder.getOrderItems().size());
        verify(productRepository, times(1)).getReferenceById(orderItem.getProduct().getId());
    }

    @Test
    public void updateOrderShouldThrowsResourceNotFoundExceptionWhenOrderIdDoesNotExist() {
        ResourceNotFoundException exception =
                Assertions.assertThrows(ResourceNotFoundException.class, () -> orderService.updateOrder(nonExistingId, orderRequestDTO));
        Assertions.assertEquals(RESOURCE_NOT_FOUND_EXCEPTION_MESSAGE, exception.getMessage());
    }

    @Test
    public void updateOrderShouldThrowsForbiddenExceptionWhenIdExistsAndOtherClientLogged() {
        doThrow(new ForbiddenException()).when(authenticationService).validateUser(any());
        ForbiddenException exception =
                Assertions.assertThrows(ForbiddenException.class, () -> orderService.updateOrder(existingId, orderRequestDTO));
        Assertions.assertEquals(FORBIDDEN_EXCEPTION_MESSAGE, exception.getMessage());
        verify(authenticationService, times(1)).validateUser(any());
    }

    @Test
    public void updateOrderShouldThrowResourceNotFoundExceptionWhenProductIdDoesNotExistAndAdminOrSelfClientLogged() {
        validOrder.getOrderItems().clear();
        orderItemDTO = new OrderItemDTO(nonExistingId, null, null, 1, null);
        orderRequestDTO = new OrderRequestDTO(List.of(orderItemDTO));
        ResourceNotFoundException exception =
                Assertions.assertThrows(ResourceNotFoundException.class, () -> orderService.updateOrder(existingId, orderRequestDTO));
        Assertions.assertEquals(RESOURCE_NOT_FOUND_EXCEPTION_MESSAGE, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource(value = "providerOfInvalidOrderStatus")
    public void updateOrderShouldThrowsOrderStatusExceptionWhenOrderStatusIsNotWaitingPaymentAndAdminOrSelfClientLogged(OrderStatus orderStatus) throws Exception {
        validOrder.setOrderStatus(orderStatus);
        OrderStatusException exception =
                Assertions.assertThrows(OrderStatusException.class, () -> orderService.updateOrder(existingId, orderRequestDTO));
        Assertions.assertEquals(ORDER_STATUS_EXCEPTION_MESSAGE, exception.getMessage());
    }

    static Stream<OrderStatus> providerOfInvalidOrderStatus() {
        return Stream.of(OrderStatus.PAID, OrderStatus.CANCELED, OrderStatus.DELIVERED, OrderStatus.SHIPPED);
    }

    @Test
    public void deleteOrderShouldDoNothingWhenOrderIdExistsAndAdminOrSelfClientLogged() {
        Assertions.assertDoesNotThrow(() -> orderService.deleteOrder(existingId));
        verify(authenticationService, times(1)).validateUser(any());
        verify(orderItemRepository, times(1)).deleteByOrderId(existingId);
        verify(orderRepository, times(1)).deleteOrderById(existingId);
    }

    @Test
    public void deleteOrderShouldThrowsResourceNotFoundExceptionWhenOrderIdDoesNotExistAndAdminOrSelfClientLogged() {
        ResourceNotFoundException exception =
                Assertions.assertThrows(ResourceNotFoundException.class, () -> orderService.deleteOrder(nonExistingId));
        Assertions.assertEquals(RESOURCE_NOT_FOUND_EXCEPTION_MESSAGE, exception.getMessage());
    }

    @Test
    public void deleteOrderShouldThrowsForbiddenExceptionWhenIdExistsAndOtherClientLogged() {
        doThrow(new ForbiddenException()).when(authenticationService).validateUser(any());
        ForbiddenException exception =
                Assertions.assertThrows(ForbiddenException.class, () -> orderService.deleteOrder(existingId));
        Assertions.assertEquals(FORBIDDEN_EXCEPTION_MESSAGE, exception.getMessage());
        verify(authenticationService, times(1)).validateUser(any());
    }

}
