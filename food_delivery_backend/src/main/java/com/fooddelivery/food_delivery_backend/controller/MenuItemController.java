package com.fooddelivery.food_delivery_backend.controller;

import com.fooddelivery.food_delivery_backend.dto.MenuItemRequest;
import com.fooddelivery.food_delivery_backend.dto.MenuItemResponse;
import com.fooddelivery.food_delivery_backend.service.MenuItemService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/menu-items")
public class MenuItemController {

    private final MenuItemService menuItemService;

    public MenuItemController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    // Public — deliberately NOT requiring ownership, since customers need
    // to view menus. (Still requires SOME valid JWT though, per our current
    // SecurityFilterChain rule of .anyRequest().authenticated() — we'll
    // revisit making GET menu endpoints fully public in a later refinement.)
    @GetMapping
    public ResponseEntity<List<MenuItemResponse>> getMenu(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuItemService.getMenuForRestaurant(restaurantId));
    }

    @PostMapping
    public ResponseEntity<MenuItemResponse> addItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request,
            Authentication authentication
    ) {
        MenuItemResponse response = menuItemService.addMenuItem(restaurantId, request, authentication.getName());
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<MenuItemResponse> updateItem(
            @PathVariable Long restaurantId, // present in URL for clean REST nesting; not used in logic below
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequest request,
            Authentication authentication
    ) {
        MenuItemResponse response = menuItemService.updateMenuItem(itemId, request, authentication.getName());
        return ResponseEntity.ok(response);
    }
}