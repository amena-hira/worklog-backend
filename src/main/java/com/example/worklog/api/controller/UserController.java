package com.example.worklog.api.controller;

import com.example.worklog.api.dto.UserDTO;
import com.example.worklog.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
     *
     * @return ResponseEntity containing a list of UserDTOs and HTTP status OK (200).
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Endpoint to retrieve a specific user by their ID.
     *
     * @param id The ID of the user.
     * @return ResponseEntity containing the UserDTO and HTTP status OK (200).
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id){
        return ResponseEntity.ok(userService.getUserById(id));
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
     * Endpoint to update an existing user.
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
