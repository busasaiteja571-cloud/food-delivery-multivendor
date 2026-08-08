package com.fooddelivery.food_delivery_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(1) // runs BEFORE JwtAuthFilter — reject abusive traffic as early as possible
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MS = 60_000; // 1 minute

    // Keyed by client IP — a real production system would use Redis here
    // instead of an in-memory map, since this map only works correctly
    // on a SINGLE server instance, not across multiple scaled instances.
    private final ConcurrentHashMap<String, RequestWindow> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Only rate-limit the sensitive auth endpoints — general browsing
        // traffic doesn't need this friction.
        if (!request.getRequestURI().startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr();
        RequestWindow window = requestCounts.computeIfAbsent(clientIp, k -> new RequestWindow());

        if (window.isExpired()) {
            window.reset();
        }

        if (window.count.incrementAndGet() > MAX_REQUESTS) {
            response.setStatus(429); // 429 Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private static class RequestWindow {
        final AtomicInteger count = new AtomicInteger(0);
        volatile long windowStart = System.currentTimeMillis();

        boolean isExpired() {
            return System.currentTimeMillis() - windowStart > WINDOW_MS;
        }

        void reset() {
            windowStart = System.currentTimeMillis();
            count.set(0);
        }
    }
}