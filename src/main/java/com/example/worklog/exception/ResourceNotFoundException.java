package com.example.worklog.exception;

/**
 * Custom exception thrown when a requested resource (like User, Project, Task) 
 * cannot be found in the database.
 * This will map to a 404 NOT FOUND HTTP status.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
