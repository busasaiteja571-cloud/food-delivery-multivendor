package com.fooddelivery.food_delivery_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fooddelivery.food_delivery_backend.dto.OrderResponse;
import com.fooddelivery.food_delivery_backend.dto.PlaceOrderRequest;
import com.fooddelivery.food_delivery_backend.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @PathVariable Long restaurantId,
            @Valid @RequestBody PlaceOrderRequest request,
            Authentication authentication
    ) {
        OrderResponse response = orderService.placeOrder(restaurantId, request, authentication.getName());
        return ResponseEntity.status(201).body(response);
    }
    
    @PatchMapping("/{orderId}/prepare")
    public ResponseEntity<OrderResponse> markPreparing(
            @PathVariable Long restaurantId,
            @PathVariable Long orderId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(orderService.markPreparing(orderId, authentication.getName()));
    }
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrdersForRestaurant(
            @PathVariable Long restaurantId, Authentication authentication) {
        return ResponseEntity.ok(
                orderService.getOrdersForRestaurant(restaurantId, authentication.getName()));
    }
    
}