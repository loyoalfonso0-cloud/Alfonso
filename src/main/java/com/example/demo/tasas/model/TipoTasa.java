package com.example.demo.tasas.model;

import lombok.Getter;

/**
 * Enum que define los diferentes tipos de tasas municipales
 */
@Getter
public enum TipoTasa {
    ASEO_URBANO("Aseo Urbano", "Tasa por recolección de basura y limpieza urbana"),
    ALUMBRADO_PUBLICO("Alumbrado Público", "Tasa por servicio de alumbrado público"),
    ORNATO_EMBELLECIMIENTO("Ornato y Embellecimiento", "Tasa por mantenimiento de espacios públicos"),
    SEGURIDAD_CIUDADANA("Seguridad Ciudadana", "Tasa por servicios de seguridad municipal"),
    BOMBEROS("Bomberos", "Tasa por servicios de bomberos y emergencias"),
    PROTECCION_CIVIL("Protección Civil", "Tasa por servicios de protección civil"),
    CEMENTERIO("Cementerio", "Tasa por servicios de cementerio municipal"),
    MERCADO_MUNICIPAL("Mercado Municipal", "Tasa por uso de espacios en mercados municipales"),
    TRANSPORTE_PUBLICO("Transporte Público", "Tasa por regulación del transporte público"),
    AMBIENTE("Ambiente", "Tasa por servicios ambientales y conservación"),
    DEPORTES_RECREACION("Deportes y Recreación", "Tasa por uso de instalaciones deportivas"),
    CULTURA("Cultura", "Tasa por servicios culturales y eventos");

    private final String descripcion;
    private final String detalle;

    TipoTasa(String descripcion, String detalle) {
        this.descripcion = descripcion;
        this.detalle = detalle;
    }
}
