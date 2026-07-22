package com.laborlaw.ragkbdemo.exception;

public class EsOperationException extends RuntimeException {

    private final int statusCode;

    public EsOperationException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
