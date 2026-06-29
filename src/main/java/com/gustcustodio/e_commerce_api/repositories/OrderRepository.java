package com.gustcustodio.e_commerce_api.repositories;

import com.gustcustodio.e_commerce_api.entities.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT order FROM Order order " +
            "JOIN FETCH order.orderItems " +
            "JOIN FETCH orderItems.id.product " +
            "WHERE order.id = :id")
    Optional<Order> findOrderById(Long id);

    @Query("SELECT order.id FROM Order order WHERE order.client.id = :id")
    Page<Long> findOrderIdsByClientId(Long id, Pageable pageable);

    @Query("SELECT order FROM Order order " +
            "JOIN FETCH order.orderItems " +
            "JOIN FETCH orderItems.id.product " +
            "WHERE order.id IN :ids")
    List<Order> findOrdersWithDetailsByIds(List<Long> ids);

    @Modifying
    @Query("DELETE FROM Order order WHERE order.id = :id")
    void deleteOrderById(Long id);

}
