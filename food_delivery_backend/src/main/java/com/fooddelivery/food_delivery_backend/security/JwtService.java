package com.fooddelivery.food_delivery_backend.security;

import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    //@Value reads a property from application.properties (or an
	//environment variable, via the ${...} syntax there) and injects it
	//here at startup - Spring, not us, decides where the actual value comes from.
	@Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;
    
    private SecretKey key; // built once, lazily, the first time it's needed

    private SecretKey getKey() {
        if (key == null) {
            key = Keys.hmacShaKeyFor(secret.getBytes());
        }
        return key;
    }
    
    // Builds a signed JWT using the user's email as the subject and 
    // embeds the role as a tamper-proof claim within the payload.
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claims(Map.of("role",role))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs)) // 24 hours
                .signWith(getKey())
                .compact();
    }
    
    // Extracts the email (the "subject") we embedded in the token when it
    // was created — this is how we figure out WHO is making the request.
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }
    
    //pulls the role back out on the receiving end.
    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // Checks the token hasn't expired. Signature validity is already
    // checked automatically inside parseClaims() — if the signature is
    // wrong or tampered with, it throws an exception before we even
    // get here.
    public boolean isTokenValid(String token) {
        return extractExpiration(token).after(new Date());
    }
    
    private Date extractExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    // Verifies the token's signature against our secret key, and decodes
    // its payload (the "claims"). If a client sent a forged or tampered
    // token, this line throws a SignatureException automatically —
    // JWT's cryptographic signature makes tampering self-evident.
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    
}