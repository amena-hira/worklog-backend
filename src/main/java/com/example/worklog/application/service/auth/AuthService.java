package com.example.worklog.application.service.auth;

import com.example.worklog.api.dto.UserDTO;
import com.example.worklog.api.dto.auth.AuthResponse;
import com.example.worklog.api.dto.auth.LoginRequest;
import com.example.worklog.api.mapper.UserMapper;
import com.example.worklog.infrastructure.persistence.entity.UserEntity;
import com.example.worklog.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Service
@RequestMapping
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Registers a new user if the email is not already taken.
     *
     * @param userDTO The user details for registration.
     * @return AuthResponse containing the registered user's email and a token.
     * @throws RuntimeException if a user with the provided email already exists.
     */
    public AuthResponse registerUser(UserDTO userDTO){
        log.info("Registering user with email: {}", userDTO.getEmail());

        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()){
            throw new RuntimeException("User with this email already exists: "+userDTO.getEmail());
        }
        UserEntity userEntity = userMapper.toEntity(userDTO);
        UserEntity savedUser = userRepository.save(userEntity);
        return new AuthResponse(savedUser.getEmail(), "token");
    }

    /**
     * Authenticates an existing user by verifying their email and password.
     *
     * @param loginRequest The login credentials (email and password).
     * @return AuthResponse containing the authenticated user's email and a token.
     * @throws RuntimeException if the email is not found or the password does not match.
     */
    public AuthResponse loginUser(LoginRequest loginRequest){
        log.info("Logging in user with email: {}", loginRequest.getEmail());
        UserEntity userEntity = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(()-> new RuntimeException("Invalid email or password"));
        if (!userEntity.getPassword().equals(loginRequest.getPassword())){
            throw new RuntimeException("Invalid email or password");
        }
        return new AuthResponse(userEntity.getEmail(), "token");
    }

}
