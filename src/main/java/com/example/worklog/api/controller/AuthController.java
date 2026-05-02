package com.example.worklog.api.controller;

import com.example.worklog.api.dto.UserDTO;
import com.example.worklog.api.dto.auth.AuthResponse;
import com.example.worklog.api.dto.auth.LoginRequest;
import com.example.worklog.application.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    /**
     * Endpoint to register a new user in the system.
     *
     * @param userDTO The user data transfer object containing registration details.
     * @return ResponseEntity containing the AuthResponse (email and token) and HTTP status OK (200).
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody UserDTO userDTO){
        return ResponseEntity.ok(authService.registerUser(userDTO));
    }
    
    /**
     * Endpoint to authenticate an existing user.
     *
     * @param loginRequest The login request object containing email and password.
     * @return ResponseEntity containing the AuthResponse (email and token) and HTTP status OK (200).
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authService.loginUser(loginRequest));
    }
}
