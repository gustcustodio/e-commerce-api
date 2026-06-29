package com.gustcustodio.e_commerce_api.repositories;

import com.gustcustodio.e_commerce_api.entities.OrderItem;
import com.gustcustodio.e_commerce_api.entities.OrderItemPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemPK> {

    @Modifying
    @Query("DELETE FROM OrderItem orderItem WHERE orderItem.id.order.id = :orderId")
    void deleteByOrderId(Long orderId);

}
