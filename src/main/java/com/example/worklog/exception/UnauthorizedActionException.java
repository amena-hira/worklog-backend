package com.example.worklog.exception;

/**
 * Custom exception thrown when a user attempts to perform an action they 
 * do not have permission for (e.g., trying to delete someone else's project).
 * This will map to a 403 FORBIDDEN HTTP status.
 */
public class UnauthorizedActionException extends RuntimeException {
    public UnauthorizedActionException(String message) {
        super(message);
    }
}
