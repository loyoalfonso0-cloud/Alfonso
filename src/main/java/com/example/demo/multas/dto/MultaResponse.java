package com.example.demo.multas.dto;

import com.example.demo.multas.model.EstadoMulta;
import com.example.demo.multas.model.TipoInfraccion;
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
public class MultaResponse {
    
    private Long id;
    private String numeroMulta;
    
    // Información del contribuyente
    private Long contribuyenteId;
    private String contribuyenteNombre;
    private String contribuyenteRif;
    
    // Información de la infracción
    private TipoInfraccion tipoInfraccion;
    private String tipoInfraccionDescripcion;
    private String descripcion;
    
    // Información financiera
    private BigDecimal monto;
    private BigDecimal montoPagado;
    private BigDecimal saldoPendiente;
    
    // Estado y fechas
    private EstadoMulta estado;
    private String estadoDescripcion;
    private LocalDateTime fechaInfraccion;
    private LocalDateTime fechaVencimiento;
    private LocalDateTime fechaPago;
    
    // Información adicional
    private String observaciones;
    private boolean estaPagada;
    private boolean estaVencida;
    private int diasVencimiento;
    
    // Auditoría
    private String usuarioRegistro;
    private LocalDateTime fechaRegistro;
    private String usuarioModificacion;
    private LocalDateTime fechaModificacion;
    
    // Métodos de conveniencia
    public String getEstadoColor() {
        return switch (estado) {
            case PENDIENTE -> "warning";
            case PAGADA -> "success";
            case PARCIALMENTE_PAGADA -> "info";
            case VENCIDA -> "danger";
            case PRESCRITA -> "secondary";
            case ANULADA -> "dark";
            case EN_RECURSO -> "primary";
        };
    }
    
    public String getEstadoBadge() {
        return switch (estado) {
            case PENDIENTE -> "badge-warning";
            case PAGADA -> "badge-success";
            case PARCIALMENTE_PAGADA -> "badge-info";
            case VENCIDA -> "badge-danger";
            case PRESCRITA -> "badge-secondary";
            case ANULADA -> "badge-dark";
            case EN_RECURSO -> "badge-primary";
        };
    }
}
