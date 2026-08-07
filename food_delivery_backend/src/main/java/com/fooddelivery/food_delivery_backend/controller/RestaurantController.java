package com.fooddelivery.food_delivery_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fooddelivery.food_delivery_backend.dto.RestaurantRequest;
import com.fooddelivery.food_delivery_backend.dto.RestaurantResponse;
import com.fooddelivery.food_delivery_backend.service.RestaurantService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @PostMapping
    public ResponseEntity<RestaurantResponse> create(
            @Valid @RequestBody RestaurantRequest request,
            // Spring automatically injects the current request's Authentication
            // object — the same one JwtAuthFilter placed into SecurityContextHolder.
            // We never parse the JWT again here; the filter already did that work.
            Authentication authentication
    ) {
        String requesterEmail = authentication.getName();
        RestaurantResponse response = restaurantService.createRestaurant(request, requesterEmail);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request,
            Authentication authentication
    ) {
        String requesterEmail = authentication.getName();
        RestaurantResponse response = restaurantService.updateRestaurant(id, request, requesterEmail);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getAll() {
        return ResponseEntity.ok(restaurantService.getAllActiveRestaurants());
    }
    
    @GetMapping("/mine")
    public ResponseEntity<List<RestaurantResponse>> getMine(Authentication authentication) {
        return ResponseEntity.ok(restaurantService.getMyRestaurants(authentication.getName()));
    }
    @GetMapping("/search")
    public ResponseEntity<List<RestaurantResponse>> search(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(restaurantService.searchRestaurants(query));
    }
    
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<RestaurantResponse> toggleStatus(
            @PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(
                restaurantService.toggleMyRestaurantStatus(id, authentication.getName()));
    }
}