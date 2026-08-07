package com.fooddelivery.food_delivery_backend.repository;

import com.fooddelivery.food_delivery_backend.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // Query derivation again: Spring Data JPA reads this method name and
    // builds: SELECT * FROM restaurants WHERE owner_id = ?
    // "Owner_UserId" navigates through the `owner` relationship to its
    // `userId` field — Hibernate handles the JOIN automatically.
    List<Restaurant> findByOwner_UserId(Long ownerId);

    // For customers browsing the marketplace — only show active vendors
    List<Restaurant> findByIsActiveTrue();
    
    // Query derivation again, but with TWO conditions chained via "And",
    // plus "ContainingIgnoreCase" — Spring translates this directly into:
    // WHERE name LIKE %query% (case-insensitive) AND is_active = true
    List<Restaurant> findByNameContainingIgnoreCaseAndIsActiveTrue(String query);
    
    List<Restaurant> findByNameContainingIgnoreCase(String query);
}