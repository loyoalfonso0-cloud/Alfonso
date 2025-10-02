package com.example.demo.multas.dto;

import com.example.demo.multas.model.TipoInfraccion;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultaRequest {
    
    @NotNull(message = "El contribuyente es obligatorio")
    private Long contribuyenteId;
    
    @NotNull(message = "El tipo de infracción es obligatorio")
    private TipoInfraccion tipoInfraccion;
    
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
    
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @Digits(integer = 13, fraction = 2, message = "El monto debe tener máximo 13 enteros y 2 decimales")
    private BigDecimal monto;
    
    @NotNull(message = "La fecha de infracción es obligatoria")
    private LocalDateTime fechaInfraccion;
    
    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDateTime fechaVencimiento;
    
    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;
    
    private String usuarioRegistro;
    
    // Validaciones personalizadas
    public boolean esFechaVencimientoValida() {
        if (fechaInfraccion != null && fechaVencimiento != null) {
            return fechaVencimiento.isAfter(fechaInfraccion);
        }
        return true;
    }
}
