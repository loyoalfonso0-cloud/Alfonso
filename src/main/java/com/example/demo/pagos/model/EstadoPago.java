package com.example.demo.pagos.model;

public enum EstadoPago {
    PENDIENTE("Pendiente"),
    PROCESADO("Procesado"),
    CONFIRMADO("Confirmado"),
    RECHAZADO("Rechazado"),
    ANULADO("Anulado"),
    DEVUELTO("Devuelto");

    private final String descripcion;

    EstadoPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
