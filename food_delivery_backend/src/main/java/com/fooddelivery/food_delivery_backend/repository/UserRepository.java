package com.fooddelivery.food_delivery_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fooddelivery.food_delivery_backend.model.User;

// Extending JpaRepository<User, Long> instantly gives us save(), findById(),
// findAll(), deleteById(), etc. — with zero implementation code written.
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA parses this method NAME to auto-generate the SQL query.
    // "findBy" + "Email" -> SELECT * FROM users WHERE email = ?
    // We never write this SQL ourselves; Hibernate derives it from the signature.
    Optional<User> findByEmail(String email);

    // Useful later for checking "is this email already registered?" during signup
    boolean existsByEmail(String email);
}