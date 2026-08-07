package com.fooddelivery.food_delivery_backend.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fooddelivery.food_delivery_backend.dto.OrderItemRequest;
import com.fooddelivery.food_delivery_backend.dto.OrderItemResponse;
import com.fooddelivery.food_delivery_backend.dto.OrderResponse;
import com.fooddelivery.food_delivery_backend.dto.PlaceOrderRequest;
import com.fooddelivery.food_delivery_backend.model.MenuItem;
import com.fooddelivery.food_delivery_backend.model.Order;
import com.fooddelivery.food_delivery_backend.model.OrderItem;
import com.fooddelivery.food_delivery_backend.model.OrderStatus;
import com.fooddelivery.food_delivery_backend.model.Restaurant;
import com.fooddelivery.food_delivery_backend.model.Role;
import com.fooddelivery.food_delivery_backend.model.User;
import com.fooddelivery.food_delivery_backend.repository.MenuItemRepository;
import com.fooddelivery.food_delivery_backend.repository.OrderRepository;
import com.fooddelivery.food_delivery_backend.repository.RestaurantRepository;
import com.fooddelivery.food_delivery_backend.repository.UserRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, MenuItemRepository menuItemRepository,
                         RestaurantRepository restaurantRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    // @Transactional: if ANYTHING in this method throws (e.g. the 3rd
    // menu item doesn't exist), Spring rolls back EVERYTHING — the Order,
    // and any OrderItems already added — so we never end up with a
    // half-created order in MySQL. All-or-nothing.
    @Transactional
    public OrderResponse placeOrder(Long restaurantId, PlaceOrderRequest request, String requesterEmail) {
        User customer = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        if (!restaurant.getIsActive()) {
            throw new IllegalArgumentException("This restaurant is not currently accepting orders");
        }

        Order order = new Order(customer, restaurant, request.deliveryAddress());

        // Runs total starts at zero; we build it up as we validate and
        // snapshot each line — this is the ONLY place totalAmount is ever set.
        BigDecimal runningTotal = BigDecimal.ZERO;

        for (OrderItemRequest lineRequest : request.items()) {
            MenuItem menuItem = menuItemRepository.findById(lineRequest.menuItemId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Menu item not found: " + lineRequest.menuItemId()));

            // Defensive check: prevent ordering a dish from a DIFFERENT
            // restaurant than the one this order is being placed against —
            // otherwise a client could mix items from restaurant A into
            // an order addressed to restaurant B.
            if (!menuItem.getRestaurant().getRestaurantId().equals(restaurantId)) {
                throw new IllegalArgumentException(
                        "Menu item " + menuItem.getName() + " does not belong to this restaurant");
            }

            if (!menuItem.getIsAvailable()) {
                throw new IllegalArgumentException(menuItem.getName() + " is currently unavailable");
            }

            // THE critical line: we read the price from the MenuItem row
            // we JUST fetched from MySQL — never from anything the client sent.
            BigDecimal lineTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(lineRequest.quantity()));
            runningTotal = runningTotal.add(lineTotal);

            OrderItem orderItem = new OrderItem(menuItem, lineRequest.quantity(), menuItem.getPrice());
            order.addItem(orderItem); // keeps both sides of the relationship in sync
        }

        order.setTotalAmount(runningTotal);
        Order savedOrder = orderRepository.save(order); // cascades to save all OrderItems too

        return toResponse(savedOrder);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(oi -> new OrderItemResponse(
                        oi.getMenuItem().getName(), oi.getQuantity(), oi.getPriceAtOrder()))
                .toList();

        return new OrderResponse(
                order.getOrderId(),
                order.getRestaurant().getName(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getDeliveryAddress(),
                order.getCreatedAt(),
                itemResponses
        );
    }
    
 // Restaurant owner moves an order from PLACED to PREPARING.
    @Transactional
    public OrderResponse markPreparing(Long orderId, String requesterEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Ownership check: is the requester the OWNER of THIS order's restaurant?
        // Same pattern as Phase 2's assertOwnership, just inlined here since
        // it's the only place this specific check is needed so far.
        if (!order.getRestaurant().getOwner().getEmail().equals(requesterEmail)) {
            throw new AccessDeniedException("You do not own this order's restaurant");
        }

        // State machine rule: can only move PLACED -> PREPARING. Trying to
        // "prepare" an already-delivered or cancelled order is invalid.
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new IllegalStateException(
                    "Cannot move to PREPARING from " + order.getStatus());
        }

        order.setStatus(OrderStatus.PREPARING);
        return toResponse(orderRepository.save(order));
    }

    // A delivery agent claims an unassigned, ready-for-pickup order.
    @Transactional
    public OrderResponse claimForDelivery(Long orderId, String requesterEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        User agent = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (agent.getRole() != Role.DELIVERY_AGENT) {
            throw new AccessDeniedException("Only delivery agents can claim orders");
        }

        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new IllegalStateException(
                    "Order must be PREPARING before it can be claimed for delivery");
        }

        // Prevents a second agent from claiming an order that's already
        // been picked up by someone else — a real concurrency concern once
        // multiple agents are active on the platform.
        if (order.getDeliveryAgent() != null) {
            throw new IllegalStateException("Order has already been claimed by another agent");
        }

        order.setDeliveryAgent(agent);
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        try {
        	//THIS is the line where the race actually gets resolved. If
        	//another transaction already incrementedd 'version' between our
        	//findbyId above and this saved(), Hibernate's generated UPDATED
        	//affects zero rows, and it throws immediately.
        	return toResponse(orderRepository.save(order));
        }catch (ObjectOptimisticLockingFailureException ex) {
            // Translate Hibernate's low-level exception into the same
            // business-meaningful exception type we already use elsewhere —
            // the Controller layer and GlobalExceptionHandler don't need to
            // know or care that THIS particular conflict came from locking
            // versus any other state conflict.
            throw new IllegalStateException("This order was just claimed by another agent — please refresh.");
        }
        
    }

    // The ASSIGNED delivery agent marks the order as delivered.
    @Transactional
    public OrderResponse markDelivered(Long orderId, String requesterEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Ownership check here means something different than in Phase 2:
        // not "do you own this restaurant" but "were YOU the specific agent
        // assigned to THIS delivery."
        if (order.getDeliveryAgent() == null ||
            !order.getDeliveryAgent().getEmail().equals(requesterEmail)) {
            throw new AccessDeniedException("You are not the assigned agent for this order");
        }

        if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY) {
            throw new IllegalStateException(
                    "Cannot mark DELIVERED from " + order.getStatus());
        }

        order.setStatus(OrderStatus.DELIVERED);
        return toResponse(orderRepository.save(order));
    }
    
    public List<OrderResponse> getAvailableOrders() {
        return orderRepository.findByStatusAndDeliveryAgentIsNull(OrderStatus.PREPARING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<OrderResponse> getMyDeliveries(String requesterEmail) {
        User agent = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return orderRepository.findByDeliveryAgent_UserId(agent.getUserId())
                .stream()
                // Only show deliveries this agent is ACTIVELY working —
                // completed/old deliveries would clutter an "active jobs" view.
                // A separate "delivery history" endpoint could relax this later.
                .filter(o -> o.getStatus() == OrderStatus.OUT_FOR_DELIVERY)
                .map(this::toResponse)
                .toList();
    }
    
    // Distinct from getMyDeliveries()  — that filters by
    // delivery_agent_id, this filters by customer_id. Same URL pattern
    // philosophy ("mine"), completely different underlying query and role.
@Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String requesterEmail) {
     User customer = userRepository.findByEmail(requesterEmail)
             .orElseThrow(() -> new IllegalArgumentException("User not found"));

     return orderRepository.findByCustomer_UserId(customer.getUserId())
             .stream()
             // Newest first — customers care most about their most
             // recent order, especially one still in progress.
             .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
             .map(this::toResponse)
             .toList();
 }
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForRestaurant(Long restaurantId, String requesterEmail) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        // Same ownership pattern as Phase 2/3 — only THIS restaurant's owner
        // can see its incoming orders, not restaurant owners in general.
        if (!restaurant.getOwner().getEmail().equals(requesterEmail)) {
            throw new AccessDeniedException("You do not own this restaurant");
        }

        return orderRepository.findByRestaurant_RestaurantId(restaurantId)
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toResponse)
                .toList();
    }
}