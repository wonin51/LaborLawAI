package com.laborlaw.ragkbdemo.exception;

public class ApiOperationException extends RuntimeException {

    private final int statusCode;

    public ApiOperationException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
