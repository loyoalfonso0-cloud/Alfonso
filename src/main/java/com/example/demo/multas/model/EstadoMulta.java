package com.example.demo.multas.model;

public enum EstadoMulta {
    PENDIENTE("Pendiente de pago"),
    PAGADA("Pagada"),
    PARCIALMENTE_PAGADA("Parcialmente pagada"),
    VENCIDA("Vencida"),
    PRESCRITA("Prescrita"),
    ANULADA("Anulada"),
    EN_RECURSO("En recurso de apelación");
    
    private final String descripcion;
    
    EstadoMulta(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public boolean permiteModificacion() {
        return this == PENDIENTE || this == PARCIALMENTE_PAGADA || this == VENCIDA;
    }
    
    public boolean permitePago() {
        return this == PENDIENTE || this == PARCIALMENTE_PAGADA || this == VENCIDA;
    }
}
