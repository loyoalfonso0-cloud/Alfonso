package com.example.demo.servicios.exception;

/**
 * Excepción personalizada para el módulo de Servicios
 */
public class ServicioException extends RuntimeException {
    
    public ServicioException(String message) {
        super(message);
    }
    
    public ServicioException(String message, Throwable cause) {
        super(message, cause);
    }
}
