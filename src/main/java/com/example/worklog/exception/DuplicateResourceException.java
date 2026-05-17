package com.example.worklog.exception;

/**
 * Custom exception thrown when attempting to create a resource that already exists 
 * (e.g., registering a user with an email that is already taken).
 * This will map to a 409 CONFLICT HTTP status.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
