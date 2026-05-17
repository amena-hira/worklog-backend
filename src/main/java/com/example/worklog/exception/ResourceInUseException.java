package com.example.worklog.exception;

/**
 * Custom exception thrown when attempting to delete a resource that is still
 * being referenced by other entities in the database (Foreign Key Constraint Violation).
 * Maps to a 409 CONFLICT HTTP status.
 */
public class ResourceInUseException extends RuntimeException {
    public ResourceInUseException(String message) {
        super(message);
    }
}
