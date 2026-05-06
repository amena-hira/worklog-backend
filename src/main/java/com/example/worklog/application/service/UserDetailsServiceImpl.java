package com.example.worklog.application.service;

import com.example.worklog.infrastructure.persistence.entity.UserEntity;
import com.example.worklog.infrastructure.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

// Marks this class as a Spring Service, so it can be automatically injected by Spring Security
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    // Injects the UserRepository to allow database access for user information
    @Autowired
    private UserRepository userRepository;

    /**
     * This method is required by Spring Security's UserDetailsService interface.
     * It loads a user's specific data (password, roles) based on their identifying "username" (which is email in this app).
     * 
     * @param email The email extracted from the login request or JWT token
     * @return A UserDetails object containing the user's credentials and granted authorities (roles)
     * @throws UsernameNotFoundException if no user exists in the database with the provided email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        // 1. Attempt to fetch the user entity from the database using their email
        // If not found, throw an exception which Spring Security will handle as an authentication failure
        UserEntity user = userRepository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("User not found!"));

        // 2. Convert our custom UserEntity into Spring Security's built-in User object
        return new User(
                user.getEmail(), // The principal (username/email)
                user.getPassword(), // The credentials (hashed password)
                // Convert the user's role (e.g., "ROLE_USER") into a Spring Security SimpleGrantedAuthority
                List.of(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}
