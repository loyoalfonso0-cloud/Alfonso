package com.example.demo.tasas.model;

import lombok.Getter;

/**
 * Enum que define los períodos de facturación de las tasas
 */
@Getter
public enum PeriodoFacturacion {
    MENSUAL("Mensual", "Facturación cada mes", 1),
    BIMESTRAL("Bimestral", "Facturación cada dos meses", 2),
    TRIMESTRAL("Trimestral", "Facturación cada tres meses", 3),
    CUATRIMESTRAL("Cuatrimestral", "Facturación cada cuatro meses", 4),
    SEMESTRAL("Semestral", "Facturación cada seis meses", 6),
    ANUAL("Anual", "Facturación anual", 12);

    private final String descripcion;
    private final String detalle;
    private final int meses;

    PeriodoFacturacion(String descripcion, String detalle, int meses) {
        this.descripcion = descripcion;
        this.detalle = detalle;
        this.meses = meses;
    }
}
