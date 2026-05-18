package com.example.worklog.api.controller;

import com.example.worklog.api.dto.AdminUserDTO;
import com.example.worklog.api.dto.UserDTO;
import com.example.worklog.application.service.users.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    /**
     * Endpoint to add a new user.
     *
     * @param userDTO The user data transfer object containing the user's details.
     * @return ResponseEntity containing the created UserDTO and HTTP status CREATED (201).
     */
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO){
        return new ResponseEntity<>(userService.createUser(userDTO), HttpStatus.CREATED);
    }

    /**
     * Endpoint to retrieve all users.
     * Both User and Admin can access this.
     * Returns AdminUserDTO which includes the user ID (needed for assigning users).
     *
     * @return ResponseEntity containing a list of AdminUserDTOs and HTTP status OK (200).
     */
    @GetMapping
    public ResponseEntity<List<AdminUserDTO>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsersForAdmin());
    }

    /**
     * Endpoint to retrieve the profile of the currently authenticated user.
     * Extracts the email from the JWT token.
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(userService.getUserByEmail(authentication.getName()));
    }

    /**
     * Endpoint to update the profile of the currently authenticated user.
     * The email of the user to update is extracted from the JWT token for security.
     *
     * @param userDTO The updated user details.
     */
    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateCurrentUser(@RequestBody UserDTO userDTO, Authentication authentication) {
        return ResponseEntity.ok(userService.updateCurrentUser(authentication.getName(), userDTO));
    }

    /**
     * Endpoint to retrieve a specific user by their ID.
     *
     * @param id The ID of the user.
     * @return ResponseEntity containing the AdminUserDTO and HTTP status OK (200).
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdminUserDTO> getUserById(@PathVariable Long id){
        return ResponseEntity.ok(userService.getUserByIdForAdmin(id));
    }

    /**
     * Endpoint to retrieve a specific user by their email address.
     *
     * @param email The email address of the user.
     * @return ResponseEntity containing the UserDTO and HTTP status OK (200).
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email){
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    /**
     * Endpoint to update an existing user by their ID.
     * Only Admin can access this.
     *
     * @param id The ID of the user to update.
     * @param userDTO The user data transfer object containing the updated details.
     * @return ResponseEntity containing the updated UserDTO and HTTP status OK (200).
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO){
        return ResponseEntity.ok(userService.updateUser(id, userDTO));
    }

    /**
     * Endpoint to delete a user by their ID.
     * Only Admin can access this.
     *
     * @param id The ID of the user to delete.
     * @return ResponseEntity with no content and HTTP status NO_CONTENT (204).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
