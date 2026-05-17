package com.example.worklog.exception;

/**
 * Custom exception thrown when a business rule is violated 
 * (e.g., trying to create a task without linking it to a project).
 * This will map to a 400 BAD REQUEST HTTP status.
 */
public class InvalidOperationException extends RuntimeException {
    public InvalidOperationException(String message) {
        super(message);
    }
}
