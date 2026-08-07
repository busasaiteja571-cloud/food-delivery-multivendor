package com.fooddelivery.food_delivery_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fooddelivery.food_delivery_backend.dto.OrderResponse;
import com.fooddelivery.food_delivery_backend.dto.RestaurantResponse;
import com.fooddelivery.food_delivery_backend.dto.UserSummaryResponse;
import com.fooddelivery.food_delivery_backend.service.AdminService;


@RestController
@RequestMapping("/api/admin")
// Applied at the CLASS level: EVERY method in this controller now
// requires the ROLE_ADMIN authority, checked BEFORE any method body runs.
// A non-admin gets a 403 automatically — no manual role check needed
// anywhere in this file.
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserSummaryResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurants() {
        return ResponseEntity.ok(adminService.getAllRestaurants());
    }

    @PatchMapping("/restaurants/{id}/toggle-status")
    public ResponseEntity<RestaurantResponse> toggleRestaurantStatus(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleRestaurantStatus(id));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(adminService.getAllOrders());
    }
}