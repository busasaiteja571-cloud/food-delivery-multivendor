package com.fooddelivery.food_delivery_backend.repository;

import com.fooddelivery.food_delivery_backend.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    // Powers "show me this restaurant's menu" — the most common query
    // a customer's screen will make.
    List<MenuItem> findByRestaurant_RestaurantId(Long restaurantId);
}