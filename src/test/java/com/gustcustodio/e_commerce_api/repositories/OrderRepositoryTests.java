package com.gustcustodio.e_commerce_api.repositories;

import com.gustcustodio.e_commerce_api.entities.Order;
import com.gustcustodio.e_commerce_api.factories.OrderFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

@DataJpaTest
public class OrderRepositoryTests {

    @Autowired
    private OrderRepository orderRepository;

    private Long existingUserId, userWithoutOrders;
    private Long existingOrderId, dependentOrderId, nonExistingOrderId;
    private Order order;
    private PageRequest pageRequest;

    @BeforeEach
    void setUp() throws Exception {
        existingUserId = 1L;
        userWithoutOrders = 4L;
        existingOrderId = 1L;
        dependentOrderId = 2L;
        nonExistingOrderId = 1000L;
        order = OrderFactory.createValidOrder();
        pageRequest = PageRequest.of(0, 5);
    }

    @Test
    public void findOrderByIdShouldReturnNotEmptyOptionalWhenIdExists() {
        Optional<Order> result = orderRepository.findOrderById(existingOrderId);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertTrue(result.get().getOrderItems().size() > 0);
    }

    @Test
    public void findOrderByIdShouldReturnEmptyOptionalWhenIdDoesNotExists() {
        Optional<Order> result = orderRepository.findOrderById(nonExistingOrderId);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void findOrderIdsByClientIdShouldReturnPageOfOrdersId() {
        Page<Long> result = orderRepository.findOrderIdsByClientId(existingUserId, pageRequest);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(0, result.getNumber());
        Assertions.assertEquals(5, result.getSize());
        Assertions.assertTrue(result.getTotalElements() > 0);
    }

    @Test
    public void findOrderIdsByClientIdShouldReturnEmptyPageWhenClientDoesntHaveOrders() {
        Page<Long> result = orderRepository.findOrderIdsByClientId(userWithoutOrders, pageRequest);
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertTrue(result.getTotalElements() == 0);
    }

    @Test
    public void findOrdersWithDetailsByIdsShouldReturnListOfOrders() {
        List<Order> result = orderRepository.findOrdersWithDetailsByIds(List.of(existingOrderId));
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals("Liev Tolstói", result.get(0).getClient().getName());
    }

    @Test
    public void findOrdersWithDetailsByIdsShouldReturnEmptyListWhenClientDoesntHaveOrders() {
        List<Order> result = orderRepository.findOrdersWithDetailsByIds(List.of(nonExistingOrderId));
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void deleteShouldDeleteOrderWhenIdExists() {
        order.setId(null);
        order.getOrderItems().clear();
        Order newOrder = orderRepository.save(order);
        orderRepository.deleteOrderById(newOrder.getId());
        Optional<Order> result = orderRepository.findById(newOrder.getId());
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void deleteOrderByIdShouldThrowDataIntegrityViolationExceptionWhenOrderStillHasItems() {
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            orderRepository.deleteOrderById(dependentOrderId);
        });
    }

}
