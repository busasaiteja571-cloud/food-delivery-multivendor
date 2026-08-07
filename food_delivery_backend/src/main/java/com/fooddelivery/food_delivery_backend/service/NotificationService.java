package com.fooddelivery.food_delivery_backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fooddelivery.food_delivery_backend.dto.NotificationResponse;
import com.fooddelivery.food_delivery_backend.model.Order;
import com.fooddelivery.food_delivery_backend.model.Restaurant;
import com.fooddelivery.food_delivery_backend.model.Role;
import com.fooddelivery.food_delivery_backend.model.User;
import com.fooddelivery.food_delivery_backend.repository.OrderRepository;
import com.fooddelivery.food_delivery_backend.repository.RestaurantRepository;
import com.fooddelivery.food_delivery_backend.repository.UserRepository;

@Service
public class NotificationService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;

    public NotificationService(UserRepository userRepository, OrderRepository orderRepository,
                                RestaurantRepository restaurantRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public List<NotificationResponse> getMyNotifications(String requesterEmail, LocalDateTime since) {
        User user = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Order> relevantOrders = switch (user.getRole()) {
            case CUSTOMER -> orderRepository.findByCustomer_UserId(user.getUserId());
            case DELIVERY_AGENT -> orderRepository.findByDeliveryAgent_UserId(user.getUserId());
            case RESTAURANT_OWNER -> getOrdersForOwner(user.getUserId());
            case ADMIN -> List.of();
        };

        return relevantOrders.stream()
                .filter(o -> o.getStatusUpdatedAt() != null && o.getStatusUpdatedAt().isAfter(since))
                .map(o -> new NotificationResponse(
                        o.getOrderId(),
                        buildMessage(o, user.getRole()),
                        o.getStatusUpdatedAt()
                ))
                .toList();
    }

    private String buildMessage(Order order, Role role) {
        return switch (role) {
            case CUSTOMER -> "Your order from " + order.getRestaurant().getName()
                    + " is now " + order.getStatus().name().replace('_', ' ');
            case RESTAURANT_OWNER -> "Order #" + order.getOrderId() + " is now "
                    + order.getStatus().name().replace('_', ' ');
            case DELIVERY_AGENT -> "Delivery #" + order.getOrderId() + " updated to "
                    + order.getStatus().name().replace('_', ' ');
            default -> "Order #" + order.getOrderId() + " updated";
        };
    }

    private List<Order> getOrdersForOwner(Long ownerId) {
        List<Restaurant> ownedRestaurants = restaurantRepository.findByOwner_UserId(ownerId);
        if (ownedRestaurants.isEmpty()) return List.of();
        Long restaurantId = ownedRestaurants.get(0).getRestaurantId();
        return orderRepository.findByRestaurant_RestaurantId(restaurantId);
    }
}