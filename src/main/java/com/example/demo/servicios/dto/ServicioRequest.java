package com.example.demo.servicios.dto;

import com.example.demo.servicios.domain.EstadoServicio;
import com.example.demo.servicios.domain.TipoServicio;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para peticiones de Servicio
 */
@Data
public class ServicioRequest {
    
    @NotNull(message = "El contribuyente es obligatorio")
    private Long contribuyenteId;
    
    @NotNull(message = "El tipo de servicio es obligatorio")
    private TipoServicio tipoServicio;
    
    private EstadoServicio estado = EstadoServicio.ACTIVO;
    
    @NotNull(message = "La tarifa base es obligatoria")
    @DecimalMin(value = "0.01", message = "La tarifa base debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "Formato de tarifa inválido")
    private BigDecimal tarifaBase;
    
    @DecimalMin(value = "0.00", message = "El consumo actual no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "Formato de consumo inválido")
    private BigDecimal consumoActual;
    
    @DecimalMin(value = "0.00", message = "El consumo anterior no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "Formato de consumo inválido")
    private BigDecimal consumoAnterior;
    
    @DecimalMin(value = "0.00", message = "El monto facturado no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "Formato de monto inválido")
    private BigDecimal montoFacturado;
    
    private LocalDate fechaInstalacion;
    
    private LocalDate fechaUltimaLectura;
    
    private LocalDate fechaProximaLectura;
    
    private LocalDate fechaCorte;
    
    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    private String direccionServicio;
    
    @Size(max = 50, message = "El medidor no puede exceder 50 caracteres")
    private String medidor;
    
    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;
    
    private String usuarioRegistro;
}
