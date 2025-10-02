package com.example.demo.tasas.model;

import lombok.Getter;

/**
 * Enum que define los estados de una tasa municipal
 */
@Getter
public enum EstadoTasa {
    ACTIVA("Activa", "La tasa está vigente y se puede facturar"),
    SUSPENDIDA("Suspendida", "La tasa está temporalmente suspendida"),
    VENCIDA("Vencida", "La tasa ha vencido y requiere pago"),
    PAGADA("Pagada", "La tasa ha sido pagada completamente"),
    PARCIALMENTE_PAGADA("Parcialmente Pagada", "La tasa tiene pagos parciales pendientes"),
    EXONERADA("Exonerada", "La tasa ha sido exonerada por resolución"),
    ANULADA("Anulada", "La tasa ha sido anulada");

    private final String descripcion;
    private final String detalle;

    EstadoTasa(String descripcion, String detalle) {
        this.descripcion = descripcion;
        this.detalle = detalle;
    }
}
