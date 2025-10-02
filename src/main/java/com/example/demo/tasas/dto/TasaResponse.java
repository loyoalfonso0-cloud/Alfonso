package com.example.demo.tasas.dto;

import com.example.demo.tasas.model.EstadoTasa;
import com.example.demo.tasas.model.PeriodoFacturacion;
import com.example.demo.tasas.model.TipoTasa;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para respuestas de tasas municipales
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TasaResponse {

    private Long id;
    private Long contribuyenteId;
    private String contribuyenteNombre;
    private String contribuyenteRif;
    private String numeroTasa;
    private TipoTasa tipoTasa;
    private String tipoTasaDescripcion;
    private String descripcion;
    private BigDecimal montoBase;
    private BigDecimal montoPagado;
    private BigDecimal saldoPendiente;
    private EstadoTasa estado;
    private String estadoDescripcion;
    private PeriodoFacturacion periodoFacturacion;
    private String periodoFacturacionDescripcion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaVencimiento;
    private LocalDateTime fechaPago;
    
    // Información de ubicación/inmueble
    private String direccion;
    private String zonaMunicipal;
    private BigDecimal areaInmueble;
    private BigDecimal valorCatastral;
    
    private String observaciones;
    private LocalDateTime fechaRegistro;
    private String usuarioRegistro;
    
    // Campos calculados
    private boolean vencida;
    private boolean completamentePagada;
    private long diasVencimiento;
}
