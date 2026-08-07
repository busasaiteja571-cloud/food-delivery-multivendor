package com.fooddelivery.food_delivery_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fooddelivery.food_delivery_backend.model.Order;
import com.fooddelivery.food_delivery_backend.model.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomer_UserId(Long customerId);
    List<Order> findByRestaurant_RestaurantId(Long restaurantId);
    List<Order> findByDeliveryAgent_UserId(Long agentId);
    // Orders ready for pickup that no agent has claimed yet — the "available
    // jobs" list every idle delivery agent needs to see.
    List<Order> findByStatusAndDeliveryAgentIsNull(OrderStatus status);
}