package com.example.worklog.application.service.users;

import com.example.worklog.api.dto.UserDTO;
import com.example.worklog.api.mapper.UserMapper;
import com.example.worklog.exception.DuplicateResourceException;
import com.example.worklog.exception.ResourceNotFoundException;
import com.example.worklog.infrastructure.persistence.entity.UserEntity;
import com.example.worklog.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a new user in the system.
     *
     * @param userDTO The DTO containing the user's information.
     * @return The created user as a DTO.
     */
    public UserDTO createUser(UserDTO userDTO){
        log.info("Adding new user with email: {}", userDTO.getEmail());

        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()){
            throw new DuplicateResourceException("User with this email already exists: "+userDTO.getEmail());
        }

        UserEntity userEntity = userMapper.toEntity(userDTO);
        userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        if (userEntity.getRole() == null || userEntity.getRole().isEmpty()){
            userEntity.setRole("ROLE_USER");
        }
        UserEntity savedUser = userRepository.save(userEntity);
        return userMapper.toDTO(savedUser);
    }

    /**
     * Retrieves a list of all users.
     *
     * @return A list of UserDTOs representing all users.
     */
    public List<UserDTO> getAllUsers(){
        log.info("Fetching all users");
        return userRepository.findAll().stream().map(userMapper::toDTO).collect(Collectors.toList());
    }

    /**
     * Retrieves a specific user by their unique ID.
     *
     * @param id The ID of the user to retrieve.
     * @return The UserDTO representing the requested user.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public UserDTO getUserById(Long id){
        log.info("Fetching user with id: {}", id);
        return userRepository.findById(id)
                .map(userMapper::toDTO)
                .orElseThrow(()-> new ResourceNotFoundException("User with id: "+id+" not found"));
    }

    /**
     * Retrieves a specific user by their email address.
     *
     * @param email The email address of the user to retrieve.
     * @return The UserDTO representing the requested user.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public UserDTO getUserByEmail(String email){
        log.info("Fetching user with email: {}", email);
        return userRepository.findByEmail(email)
                .map(userMapper::toDTO)
                .orElseThrow(()-> new ResourceNotFoundException("User with email: "+email+" not found"));
    }

    /**
     * Updates an existing user's information based on their ID.
     * Note: Typically used by admins to update any user.
     *
     * @param id The ID of the user to update.
     * @param userDTO The DTO containing the updated user information.
     * @return The updated user as a DTO.
     * @throws ResourceNotFoundException if the user to update is not found.
     */
    public UserDTO updateUser(Long id, UserDTO userDTO){
        log.info("Updating user with id: {}", id);
        UserEntity existingUser = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User with id: "+id+" not found"));
        return performUserUpdate(existingUser, userDTO);
    }

    /**
     * Updates the information of the currently authenticated user based on their email.
     *
     * @param currentEmail The email of the currently authenticated user.
     * @param userDTO The DTO containing the updated user information.
     * @return The updated user as a DTO.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public UserDTO updateCurrentUser(String currentEmail, UserDTO userDTO) {
        log.info("Updating current authenticated user with email: {}", currentEmail);
        UserEntity existingUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentEmail));
        
        // Prevent users from changing their own role (security measure)
        userDTO.setRole(existingUser.getRole());
        
        return performUserUpdate(existingUser, userDTO);
    }

    /**
     * Helper method to perform the actual update fields mapping and saving.
     */
    private UserDTO performUserUpdate(UserEntity existingUser, UserDTO userDTO) {
        existingUser.setFirst_name(userDTO.getFirst_name());
        existingUser.setLast_name(userDTO.getLast_name());
        
        // Check if email is being changed and if it's already taken
        if (!existingUser.getEmail().equals(userDTO.getEmail())) {
            if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
                throw new DuplicateResourceException("Email is already in use: " + userDTO.getEmail());
            }
            existingUser.setEmail(userDTO.getEmail());
        }
        
        existingUser.setGender(userDTO.getGender());
        existingUser.setRole(userDTO.getRole());
        
        // Only update password if a new one is provided
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        UserEntity updatedUser = userRepository.save(existingUser);
        return userMapper.toDTO(updatedUser);
    }

    /**
     * Deletes a user from the system by their unique ID.
     *
     * @param id The ID of the user to delete.
     * @throws ResourceNotFoundException if the user to delete is not found.
     */
    public void deleteUser(Long id){
        log.info("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)){
            throw new ResourceNotFoundException("User with id: "+id+" not found");
        }
        userRepository.deleteById(id);
    }

}
