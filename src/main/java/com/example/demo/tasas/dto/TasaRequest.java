package com.example.demo.tasas.dto;

import com.example.demo.tasas.model.EstadoTasa;
import com.example.demo.tasas.model.PeriodoFacturacion;
import com.example.demo.tasas.model.TipoTasa;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para crear y actualizar tasas municipales
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TasaRequest {

    @NotNull(message = "El contribuyente es obligatorio")
    private Long contribuyenteId;

    @NotNull(message = "El tipo de tasa es obligatorio")
    private TipoTasa tipoTasa;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotNull(message = "El monto base es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto base debe ser mayor a 0")
    private BigDecimal montoBase;

    @NotNull(message = "El período de facturación es obligatorio")
    private PeriodoFacturacion periodoFacturacion;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime fechaInicio;

    private LocalDateTime fechaVencimiento;

    // Información de ubicación/inmueble
    private String direccion;
    private String zonaMunicipal;
    private BigDecimal areaInmueble;
    private BigDecimal valorCatastral;

    private String observaciones;
    private String usuarioRegistro;
}
