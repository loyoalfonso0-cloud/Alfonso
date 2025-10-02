package com.example.demo.transacciones.model;

public enum EstadoTransaccion {
    PENDIENTE("Pendiente"),
    PROCESANDO("Procesando"),
    PROCESADA("Procesada"),
    CONFIRMADA("Confirmada"),
    ANULADA("Anulada"),
    RECHAZADA("Rechazada"),
    REVERTIDA("Revertida");

    private final String descripcion;

    EstadoTransaccion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
