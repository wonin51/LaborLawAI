package com.laborlaw.ragkbdemo.exception;

/**
 * Raised when a request to an admin endpoint cannot be authenticated.
 */
public class AdminAuthException extends RuntimeException {

    private final int statusCode;

    public AdminAuthException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}