package com.example.worklog.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

// Marks this class as a Spring-managed component, allowing it to be injected where needed
@Component
public class JwtUtil {
    
    // Injects the secret key from the application properties/environment (jwt.secret)
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Generates a secure SecretKey object from the raw secret string.
     * This key is used for both signing and verifying the JWTs.
     * 
     * @return A cryptographic SecretKey based on the HMAC-SHA algorithm
     */
    private SecretKey getSigningKey(){
        // Converts the string secret into bytes and creates an HMAC key for secure signing
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a new JSON Web Token for the authenticated user.
     * 
     * @param email The user's email address to be embedded inside the token
     * @return The fully constructed JWT string
     */
    public String generateToken(String email){
        return Jwts.builder()
                .subject(email) // Sets the 'sub' (subject) claim to the user's email
                .issuedAt(new Date()) // Sets the 'iat' (issued at) claim to the current time
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // Token expires in 1 hour
                .signWith(getSigningKey()) // Digitally signs the token using the secret key
                .compact(); // Builds the token and converts it into a URL-safe string format
    }

    /**
     * Extracts the user's email (subject) from a given token.
     * 
     * @param token The JWT string provided by the user
     * @return The email extracted from the token's payload
     */
    public String extractEmail(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey()) // Configures the parser to verify the token's signature using our secret
                .build() // Builds the JwtParser instance
                .parseSignedClaims(token) // Parses and validates the token (throws exception if invalid or tampered)
                .getPayload() // Retrieves the token's payload (claims section)
                .getSubject(); // Gets the 'sub' (subject) claim, which contains the email
    }

    /**
     * Validates whether a given token belongs to a specific user and is still active.
     * 
     * @param token The JWT string to validate
     * @param userDetails The Spring Security UserDetails object representing the current user
     * @return true if the token is valid for this user, false otherwise
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractEmail(token); // Extracts the email from the token
        // Checks if the token's email matches the user's email AND if the token is not expired
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Checks if a token has passed its expiration time.
     * 
     * @param token The JWT string to check
     * @return true if the token is expired, false if it is still valid
     */
    private boolean isTokenExpired(String token) {
        // Parses the token to extract the expiration date claim
        Date expiration = Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
                        .getExpiration();

        // Checks if the expiration date is strictly before the current date/time
        return expiration.before(new Date());
    }

}
