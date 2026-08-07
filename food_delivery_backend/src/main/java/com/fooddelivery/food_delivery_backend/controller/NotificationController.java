package com.fooddelivery.food_delivery_backend.controller;

import com.fooddelivery.food_delivery_backend.dto.NotificationResponse;
import com.fooddelivery.food_delivery_backend.service.NotificationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            // Comes in as a URL query param, e.g. ?since=2026-08-06T10:15:00
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since,
            Authentication authentication
    ) {
        return ResponseEntity.ok(notificationService.getMyNotifications(authentication.getName(), since));
    }
}