package com.example.demo.servicios.domain;

/**
 * Enumeración de estados de servicios municipales
 */
public enum EstadoServicio {
    ACTIVO("Activo", "success"),
    SUSPENDIDO("Suspendido", "warning"),
    CORTADO("Cortado", "danger"),
    INACTIVO("Inactivo", "secondary"),
    MANTENIMIENTO("En Mantenimiento", "info"),
    PENDIENTE_INSTALACION("Pendiente Instalación", "primary");

    private final String descripcion;
    private final String colorClass;

    EstadoServicio(String descripcion, String colorClass) {
        this.descripcion = descripcion;
        this.colorClass = colorClass;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getColorClass() {
        return colorClass;
    }

    /**
     * Verifica si el servicio está operativo
     */
    public boolean esOperativo() {
        return this == ACTIVO || this == MANTENIMIENTO;
    }

    /**
     * Verifica si el servicio permite facturación
     */
    public boolean permiteFacturacion() {
        return this == ACTIVO || this == SUSPENDIDO;
    }

    /**
     * Verifica si el servicio puede ser cortado
     */
    public boolean puedeSerCortado() {
        return this == ACTIVO || this == SUSPENDIDO;
    }

    /**
     * Verifica si el servicio puede ser reactivado
     */
    public boolean puedeSerReactivado() {
        return this == SUSPENDIDO || this == CORTADO || this == INACTIVO;
    }
}
