package com.fooddelivery.food_delivery_backend.service;

import com.fooddelivery.food_delivery_backend.dto.*;
import com.fooddelivery.food_delivery_backend.model.*;
import com.fooddelivery.food_delivery_backend.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;

    public AdminService(UserRepository userRepository, RestaurantRepository restaurantRepository,
                         OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.orderRepository = orderRepository;
    }

    public List<UserSummaryResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserSummaryResponse(u.getUserId(), u.getFullName(), u.getEmail(), u.getRole().name()))
                .toList();
    }

    public List<RestaurantResponse> getAllRestaurants() {
        // findAll(), not findByIsActiveTrue() — admins deliberately see
        // EVERY restaurant, active or not, unlike the public browsing
        // endpoint from Phase 5.
        return restaurantRepository.findAll().stream()
                .map(r -> new RestaurantResponse(
                        r.getRestaurantId(), r.getName(), r.getDescription(), r.getAddress(), r.getIsActive(),r.getImageUrl()))
                .toList();
    }

    public RestaurantResponse toggleRestaurantStatus(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        // Admin override: unlike Phase 2's ownership-gated update, this
        // action requires ZERO ownership check — @PreAuthorize on the
        // Controller already confirmed "this caller is an admin," which
        // is sufficient authority to act on ANY restaurant.
        restaurant.setIsActive(!restaurant.getIsActive());
        Restaurant saved = restaurantRepository.save(restaurant);

        return new RestaurantResponse(
                saved.getRestaurantId(), saved.getName(), saved.getDescription(),
                saved.getAddress(), saved.getIsActive(),saved.getImageUrl());
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toOrderResponse)
                .toList();
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(oi -> new OrderItemResponse(oi.getMenuItem().getName(), oi.getQuantity(), oi.getPriceAtOrder()))
                .toList();

        return new OrderResponse(
                order.getOrderId(), order.getRestaurant().getName(), order.getStatus().name(),
                order.getTotalAmount(), order.getDeliveryAddress(), order.getCreatedAt(), items);
    }
}