package com.example.worklog.application.service.auth;

import com.example.worklog.api.dto.UserDTO;
import com.example.worklog.api.dto.auth.AuthResponse;
import com.example.worklog.api.dto.auth.LoginRequest;
import com.example.worklog.api.mapper.UserMapper;
import com.example.worklog.config.jwt.JwtUtil;
import com.example.worklog.exception.DuplicateResourceException;
import com.example.worklog.exception.ResourceNotFoundException;
import com.example.worklog.infrastructure.persistence.entity.UserEntity;
import com.example.worklog.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user, hashes their password, and assigns a default role.
     *
     * @param userDTO The user details for registration.
     * @return AuthResponse containing the registered user's email, a JWT token, and their role.
     * @throws DuplicateResourceException if a user with the provided email already exists.
     */
    public AuthResponse registerUser(UserDTO userDTO){
        log.info("Registering user with email: {}", userDTO.getEmail());

        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()){
            throw new DuplicateResourceException("User with this email already exists: "+userDTO.getEmail());
        }
        UserEntity userEntity = userMapper.toEntity(userDTO);
        userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        userEntity.setRole("ROLE_USER");
        UserEntity savedUser = userRepository.save(userEntity);
        String token = jwtUtil.generateToken(savedUser.getEmail());
        return new AuthResponse(savedUser.getEmail(), token, savedUser.getRole());
    }

    /**
     * Authenticates an existing user by verifying their email and password using the AuthenticationManager.
     *
     * @param loginRequest The login credentials (email and password).
     * @return AuthResponse containing the authenticated user's email, a new JWT token, and their role.
     * @throws org.springframework.security.core.AuthenticationException if credentials are invalid.
     */
    public AuthResponse loginUser(LoginRequest loginRequest){
        log.info("Logging in user with email: {}", loginRequest.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        String token = jwtUtil.generateToken(loginRequest.getEmail());

        UserEntity userEntity = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(()-> new ResourceNotFoundException("User not found after successful authentication"));

        if (token != null){
            log.info("Token generated successfully");
        }else{
            log.info("Failed login attempt!");
        }

        return new AuthResponse(userEntity.getEmail(), token, userEntity.getRole());
    }

}
