package com.example.demo.multas.model;

public enum TipoInfraccion {
    DECLARACION_TARDIA("Declaración fuera de plazo"),
    PAGO_TARDIO("Pago fuera de plazo"),
    EVASION_FISCAL("Evasión fiscal"),
    DOCUMENTOS_FALSOS("Documentos falsos o alterados"),
    NO_DECLARACION("No presentación de declaración"),
    INCUMPLIMIENTO_REQUERIMIENTO("Incumplimiento de requerimiento"),
    OBSTRUCCION_FISCALIZACION("Obstrucción a la fiscalización"),
    REGISTRO_INCORRECTO("Registro incorrecto de operaciones"),
    PATENTE_VENCIDA("Patente comercial vencida"),
    CONSTRUCCION_SIN_PERMISO("Construcción sin permiso"),
    USO_SUELO_INDEBIDO("Uso de suelo indebido"),
    OTROS("Otras infracciones");
    
    private final String descripcion;
    
    TipoInfraccion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public String getCodigoInfraccion() {
        return this.name().substring(0, 3) + String.format("%02d", this.ordinal() + 1);
    }
}
