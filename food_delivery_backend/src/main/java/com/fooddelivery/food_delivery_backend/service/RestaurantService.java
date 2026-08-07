package com.fooddelivery.food_delivery_backend.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.fooddelivery.food_delivery_backend.dto.RestaurantRequest;
import com.fooddelivery.food_delivery_backend.dto.RestaurantResponse;
import com.fooddelivery.food_delivery_backend.model.Restaurant;
import com.fooddelivery.food_delivery_backend.model.User;
import com.fooddelivery.food_delivery_backend.repository.RestaurantRepository;
import com.fooddelivery.food_delivery_backend.repository.UserRepository;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    public RestaurantService(RestaurantRepository restaurantRepository, UserRepository userRepository) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    // requesterEmail comes from the JWT — this is the caller's proven
    // identity, extracted upstream in the Controller from SecurityContextHolder.
    public RestaurantResponse createRestaurant(RestaurantRequest request, String requesterEmail) {
        User owner = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Business rule: only a RESTAURANT_OWNER account can create a
        // restaurant. A CUSTOMER or DELIVERY_AGENT hitting this endpoint
        // (even with a valid JWT) gets blocked here, not just by role
        // annotations — this keeps the rule explicit and testable.
        if (owner.getRole() != com.fooddelivery.food_delivery_backend.model.Role.RESTAURANT_OWNER) {
            throw new AccessDeniedException("Only restaurant owners can create restaurants");
        }

        Restaurant restaurant = new Restaurant(owner, request.name(), request.description(), request.address());
        Restaurant saved = restaurantRepository.save(restaurant);

        return toResponse(saved);
    }

    public RestaurantResponse updateRestaurant(Long restaurantId, RestaurantRequest request, String requesterEmail) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        // THE core ownership check: does the JWT's identity match the
        // actual owner stored on this specific restaurant row? Being
        // "a" restaurant owner isn't enough — you must own THIS one.
        if (!restaurant.getOwner().getEmail().equals(requesterEmail)) {
            throw new AccessDeniedException("You do not own this restaurant");
        }

        restaurant.setName(request.name());
        restaurant.setDescription(request.description());
        restaurant.setAddress(request.address());

        Restaurant updated = restaurantRepository.save(restaurant);
        return toResponse(updated);
    }
    public List<RestaurantResponse> searchRestaurants(String query) {
        // Guard against an empty/blank search — treat it the same as
        // "browse everything active," rather than running a pointless
        // LIKE '%%' query that technically matches everything anyway,
        // but less clearly expresses the intent.
        if (query == null || query.isBlank()) {
            return getAllActiveRestaurants();
        }
        return restaurantRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(query)
                .stream()
                .map(this::toResponse)
                .toList();
    }
    
    // Public browsing endpoint — any visitor (even unauthenticated, once we
    // loosen that specific route later) should be able to see active vendors.
    public List<RestaurantResponse> getAllActiveRestaurants() {
    		return restaurantRepository.findByIsActiveTrue()
    									.stream()
    									.map(this::toResponse)
    									.toList();
    }
    // Finds the restaurant(s) belonging to whoever is making the request —
    // used by the owner's dashboard to discover "my restaurant" without
    // the frontend needing to already know its ID.
    public List<RestaurantResponse> getMyRestaurants(String requesterEmail) {
    	   User owner = userRepository.findByEmail(requesterEmail)
    	            					.orElseThrow(() -> new IllegalArgumentException("User not found"));

    	   return restaurantRepository.findByOwner_UserId(owner.getUserId())
    	            .stream()
    	            .map(this::toResponse)
    	            .toList();
    }
    
    public RestaurantResponse toggleMyRestaurantStatus(Long restaurantId, String requesterEmail) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        // Same ownership check pattern as updateRestaurant — an owner can
        // only open/close THEIR OWN restaurant, not any restaurant on the
        // platform. This is deliberately a SEPARATE method from the admin
        // version in AdminService, which skips this check entirely.
        if (!restaurant.getOwner().getEmail().equals(requesterEmail)) {
            throw new AccessDeniedException("You do not own this restaurant");
        }

        restaurant.setIsActive(!restaurant.getIsActive());
        return toResponse(restaurantRepository.save(restaurant));
    }
    
    // Small private helper so we don't repeat this mapping logic in
    // every method that needs to return a RestaurantResponse.
    private RestaurantResponse toResponse(Restaurant r) {
    	return new RestaurantResponse(
               r.getRestaurantId(), r.getName(), r.getDescription(), r.getAddress(), r.getIsActive()
    			);
    }
}