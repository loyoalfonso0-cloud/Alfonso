package com.example.demo.servicios.domain;

/**
 * Enumeración de tipos de servicios municipales
 */
public enum TipoServicio {
    AGUA_POTABLE("Agua Potable"),
    ALCANTARILLADO("Alcantarillado"),
    ASEO_URBANO("Aseo Urbano"),
    ALUMBRADO_PUBLICO("Alumbrado Público"),
    GAS_DOMESTICO("Gas Doméstico"),
    TELEFONIA("Telefonía"),
    INTERNET("Internet"),
    TELEVISION_CABLE("Televisión por Cable"),
    MANTENIMIENTO_VIAL("Mantenimiento Vial"),
    SEGURIDAD_CIUDADANA("Seguridad Ciudadana"),
    BOMBEROS("Bomberos"),
    CEMENTERIO("Cementerio"),
    MERCADO_MUNICIPAL("Mercado Municipal"),
    TRANSPORTE_PUBLICO("Transporte Público"),
    PARQUES_JARDINES("Parques y Jardines"),
    OTROS("Otros Servicios");

    private final String descripcion;

    TipoServicio(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Verifica si el servicio requiere medidor
     */
    public boolean requiereMedidor() {
        return this == AGUA_POTABLE || 
               this == GAS_DOMESTICO || 
               this == TELEFONIA || 
               this == INTERNET;
    }

    /**
     * Verifica si el servicio se factura por consumo
     */
    public boolean esFacturablePorConsumo() {
        return this == AGUA_POTABLE || 
               this == GAS_DOMESTICO || 
               this == TELEFONIA || 
               this == INTERNET ||
               this == TELEVISION_CABLE;
    }

    /**
     * Verifica si el servicio es de tarifa fija
     */
    public boolean esTarifaFija() {
        return !esFacturablePorConsumo();
    }
}
