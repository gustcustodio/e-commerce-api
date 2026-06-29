package com.gustcustodio.e_commerce_api.services;

import com.gustcustodio.e_commerce_api.dtos.OrderItemDTO;
import com.gustcustodio.e_commerce_api.dtos.OrderRequestDTO;
import com.gustcustodio.e_commerce_api.dtos.OrderResponseDTO;
import com.gustcustodio.e_commerce_api.entities.*;
import com.gustcustodio.e_commerce_api.repositories.OrderItemRepository;
import com.gustcustodio.e_commerce_api.repositories.OrderRepository;
import com.gustcustodio.e_commerce_api.repositories.ProductRepository;
import com.gustcustodio.e_commerce_api.services.exceptions.OrderStatusException;
import com.gustcustodio.e_commerce_api.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class OrderService {

    private OrderRepository orderRepository;
    private ProductRepository productRepository;
    private OrderItemRepository orderItemRepository;
    private AuthenticationService authenticationService;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, OrderItemRepository orderItemRepository, AuthenticationService authenticationService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.authenticationService = authenticationService;
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO findOrderById(Long id) {
        Order order = orderRepository.findOrderById(id).orElseThrow(ResourceNotFoundException::new);
        authenticationService.validateUser(order.getClient().getId());
        return new OrderResponseDTO(order);
    }


    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> findAllOrders(Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        User user = authenticationService.loggedUser();

        Page<Long> ordersIdPage = orderRepository.findOrderIdsByClientId(user.getId(), pageRequest);

        if (ordersIdPage.isEmpty()) return Page.empty(pageRequest);

        List<Order> orders = orderRepository.findOrdersWithDetailsByIds(ordersIdPage.getContent());
        List<OrderResponseDTO> dtos = orders.stream().map(OrderResponseDTO::new).toList();

        return new PageImpl<>(dtos, pageRequest, ordersIdPage.getTotalElements());
    }

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) {
        User user = authenticationService.loggedUser();
        Order order = new Order();
        order.setClient(user);
        order.setOrderDate(Instant.now());
        order.setOrderStatus(OrderStatus.WAITING_PAYMENT);

        for (OrderItemDTO orderItemDTO : orderRequestDTO.orderItems()) {
            createNewOrderItem(order, orderItemDTO);
        }

        orderRepository.save(order);
        orderItemRepository.saveAll(order.getOrderItems());

        return new OrderResponseDTO(order);
    }

    @Transactional
    public OrderResponseDTO updateOrder(Long id, OrderRequestDTO orderRequestDTO) {
        Order order = orderRepository.findOrderById(id).orElseThrow(ResourceNotFoundException::new);
        authenticationService.validateUser(order.getClient().getId());

        checkOrderStatus(order);

        for (OrderItemDTO orderItemDTO : orderRequestDTO.orderItems()) {
            OrderItem existingOrderItem = order.getOrderItems()
                    .stream()
                    .filter(item -> item.getProduct().getId().equals(orderItemDTO.productId()))
                    .findFirst()
                    .orElse(null);

            if (existingOrderItem != null) {
                existingOrderItem.setQuantity(orderItemDTO.quantity());
            } else {
                createNewOrderItem(order, orderItemDTO);
            }
        }

        return new OrderResponseDTO(order);
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        checkOrderStatus(order);
        authenticationService.validateUser(order.getClient().getId());
        orderItemRepository.deleteByOrderId(order.getId());
        orderRepository.deleteOrderById(order.getId());
    }

    private void checkOrderStatus(Order order) {
        if (order.getOrderStatus() != OrderStatus.WAITING_PAYMENT) {
            throw new OrderStatusException();
        }
    }

    private void createNewOrderItem(Order order, OrderItemDTO orderItemDTO) {
        try {
            Product product = productRepository.getReferenceById(orderItemDTO.productId());
            OrderItem newItem = new OrderItem(order, product, orderItemDTO.quantity(), product.getPrice());
            order.getOrderItems().add(newItem);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException();
        }
    }

}
