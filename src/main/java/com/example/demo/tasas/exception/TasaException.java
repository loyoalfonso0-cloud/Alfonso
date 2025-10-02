package com.example.demo.tasas.exception;

/**
 * Excepción personalizada para el módulo de tasas municipales
 */
public class TasaException extends RuntimeException {
    
    public TasaException(String message) {
        super(message);
    }
    
    public TasaException(String message, Throwable cause) {
        super(message, cause);
    }
}
