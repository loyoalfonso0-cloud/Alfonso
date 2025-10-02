package com.example.demo.multas.exception;

public class MultaException extends RuntimeException {
    
    public MultaException(String message) {
        super(message);
    }
    
    public MultaException(String message, Throwable cause) {
        super(message, cause);
    }
}
