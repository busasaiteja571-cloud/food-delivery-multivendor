package com.fooddelivery.food_delivery_backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // nullable = true (the default) is intentional here: no agent is
    // assigned yet when an order is first placed.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_agent_id")
    private User deliveryAgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 8, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // @OneToMany: the INVERSE side of OrderItem's @ManyToOne back to Order.
    // "mappedBy" tells Hibernate: "the foreign key already lives on the
    // OTHER table (order_items.order_id) — don't create a duplicate
    // join column here." cascade = ALL means saving an Order also saves
    // any OrderItems attached to it in the same operation.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    // @Version: Hibernate automatically manages this field. Every UPDATE to
    // this row silently includes "AND version = <the value we read>" in its
    // WHERE clause, and increments it by 1 on success. We never set this
    // field ourselves — Hibernate owns it entirely.
    @Version
    @Column(name = "version")
    private Long version;
    
    // Tracks the last time THIS row's status changed — updated only inside
    // setStatus(), never touched anywhere else. This lets us answer "did
    // anything change recently" without a separate notifications table.
    @Column(name = "status_updated_at")
    private LocalDateTime statusUpdatedAt;

    public Order() {}
    
    public Order(User customer, Restaurant restaurant, String deliveryAddress) {
        this.customer = customer;
        this.restaurant = restaurant;
        this.deliveryAddress = deliveryAddress;
        this.status = OrderStatus.PLACED;
        this.totalAmount = BigDecimal.ZERO; // set properly once items are added
        this.createdAt = LocalDateTime.now();
    }

    // Helper that keeps BOTH sides of the relationship in sync — see
    // anatomy section below for why this matters.
    public void addItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    public Long getOrderId() { return orderId; }
    public User getCustomer() { return customer; }
    public Restaurant getRestaurant() { return restaurant; }
    public User getDeliveryAgent() { return deliveryAgent; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OrderItem> getOrderItems() { return orderItems; }
    public LocalDateTime getStatusUpdatedAt() { return statusUpdatedAt; } // ADD THIS LINE
    
    // No getter needed for normal business logic, but harmless to add if
    // you want it visible for debugging:
    public Long getVersion() { return version; }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.statusUpdatedAt = LocalDateTime.now();
    }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setDeliveryAgent(User deliveryAgent) { this.deliveryAgent = deliveryAgent; }
}