package com.fooddelivery.food_delivery_backend.service;

import com.fooddelivery.food_delivery_backend.dto.MenuItemRequest;
import com.fooddelivery.food_delivery_backend.dto.MenuItemResponse;
import com.fooddelivery.food_delivery_backend.model.MenuItem;
import com.fooddelivery.food_delivery_backend.model.Restaurant;
import com.fooddelivery.food_delivery_backend.repository.MenuItemRepository;
import com.fooddelivery.food_delivery_backend.repository.RestaurantRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    public MenuItemService(MenuItemRepository menuItemRepository, RestaurantRepository restaurantRepository) {
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public MenuItemResponse addMenuItem(Long restaurantId, MenuItemRequest request, String requesterEmail) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        // Same ownership pattern as RestaurantService, but walking through
        // the restaurant to reach its owner — a MenuItem has no owner
        // field of its own, so we borrow its parent restaurant's identity.
        assertOwnership(restaurant, requesterEmail);

        MenuItem item = new MenuItem(restaurant, request.name(), request.description(), request.price());
        MenuItem saved = menuItemRepository.save(item);

        return toResponse(saved);
    }

    public MenuItemResponse updateMenuItem(Long itemId, MenuItemRequest request, String requesterEmail) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));

        // Note: we check ownership via item.getRestaurant(), NOT by
        // re-fetching the restaurant separately — Hibernate's LAZY loading
        // means this triggers a query only now, the moment we actually
        // need it (getOwner() call below).
        assertOwnership(item.getRestaurant(), requesterEmail);

        item.setName(request.name());
        item.setDescription(request.description());
        item.setPrice(request.price());

        return toResponse(menuItemRepository.save(item));
    }

    // Public endpoint — no ownership check needed, since any customer
    // should be able to view any restaurant's menu.
    public List<MenuItemResponse> getMenuForRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurant_RestaurantId(restaurantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Extracted into its own method since BOTH addMenuItem and
    // updateMenuItem need the identical ownership rule — avoids
    // duplicating this security-critical logic in two places.
    private void assertOwnership(Restaurant restaurant, String requesterEmail) {
        if (!restaurant.getOwner().getEmail().equals(requesterEmail)) {
            throw new AccessDeniedException("You do not own this restaurant's menu");
        }
    }

    private MenuItemResponse toResponse(MenuItem item) {
        return new MenuItemResponse(
                item.getItemId(), item.getName(), item.getDescription(),
                item.getPrice(), item.getIsAvailable(),item.getImageUrl()
        );
    }
}