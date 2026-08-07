package com.fooddelivery.food_delivery_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fooddelivery.food_delivery_backend.dto.OrderResponse;
import com.fooddelivery.food_delivery_backend.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class DeliveryController {

    private final OrderService orderService;

    public DeliveryController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PatchMapping("/{orderId}/claim")
    public ResponseEntity<OrderResponse> claim(
            @PathVariable Long orderId, Authentication authentication) {
        return ResponseEntity.ok(orderService.claimForDelivery(orderId, authentication.getName()));
    }

    @PatchMapping("/{orderId}/deliver")
    public ResponseEntity<OrderResponse> deliver(
            @PathVariable Long orderId, Authentication authentication) {
        return ResponseEntity.ok(orderService.markDelivered(orderId, authentication.getName()));
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<OrderResponse>> getAvailable() {
        return ResponseEntity.ok(orderService.getAvailableOrders());
    }

    @GetMapping("/mine")
    public ResponseEntity<List<OrderResponse>> getMine(Authentication authentication) {
        return ResponseEntity.ok(orderService.getMyDeliveries(authentication.getName()));
    }
}