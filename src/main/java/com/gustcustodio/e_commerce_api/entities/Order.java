package com.gustcustodio.e_commerce_api.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "tb_order")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TIMESTAMP WITHOUT TIME ZONE")
    private Instant orderDate;

    private OrderStatus orderStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private User client;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "id.order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> orderItems = new HashSet<>();

    public Order() {
    }

    public Order(Long id, Instant orderDate, OrderStatus orderStatus, User client) {
        this.id = id;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.client = client;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Instant orderDate) {
        this.orderDate = orderDate;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public User getClient() {
        return client;
    }

    public void setClient(User client) {
        this.client = client;
    }

    public Set<OrderItem> getOrderItems() {
        return orderItems;
    }

    public List<Product> getProducts() {
        return orderItems.stream().map(orderItem -> orderItem.getProduct()).toList();
    }

    public Double getTotalOrderPrice() {
        Double total = 0.0;
        for (OrderItem item : orderItems) {
            total += item.getSubTotalOrderItemPrice();
        }
        return BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;
        return Objects.equals(getId(), order.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderDate=" + orderDate +
                ", orderStatus=" + orderStatus +
                ", client=" + client +
                '}';
    }

}
