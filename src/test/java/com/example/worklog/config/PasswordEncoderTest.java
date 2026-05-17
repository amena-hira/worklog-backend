package com.example.worklog.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the PasswordEncoder to ensure passwords are securely hashed
 * and validated correctly before storing them in the database.
 */
class PasswordEncoderTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // We instantiate the BCryptPasswordEncoder just like it is configured in SecurityConfig.java
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Test
    @DisplayName("Success: Encoded password should match the original raw password")
    void verifyPasswordMatch_Success() {
        // Arrange
        String rawPassword = "MySecurePassword123!";
        
        // Act
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Assert
        // Verify that the encoded password is not just the raw password in plain text
        assertFalse(encodedPassword.equals(rawPassword), "Encoded password should not be plain text");
        
        // Verify that the encoder can successfully match the raw password against the hashed version
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword), "Raw password should match the encoded password");
    }

    @Test
    @DisplayName("Failure: Incorrect raw password should NOT match the encoded password")
    void verifyPasswordMatch_Failure() {
        // Arrange
        String rawPassword = "MySecurePassword123!";
        String wrongPassword = "WrongPassword456!";
        
        // Act
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Assert
        // Verify that attempting to match a different password fails
        assertFalse(passwordEncoder.matches(wrongPassword, encodedPassword), "Wrong password should not match the encoded password");
    }
}
