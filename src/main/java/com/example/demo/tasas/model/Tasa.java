package com.example.demo.tasas.model;

import com.example.demo.tributario.model.Contribuyente;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa una tasa municipal
 */
@Entity
@Table(name = "tasas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tasa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    @NotNull(message = "El contribuyente es obligatorio")
    private Contribuyente contribuyente;

    @Column(name = "numero_tasa", unique = true, nullable = false, length = 50)
    @NotBlank(message = "El número de tasa es obligatorio")
    private String numeroTasa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tasa", nullable = false, length = 50)
    @NotNull(message = "El tipo de tasa es obligatorio")
    private TipoTasa tipoTasa;

    @Column(nullable = false, length = 500)
    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull(message = "El monto base es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto base debe ser mayor a 0")
    private BigDecimal montoBase;

    @Column(name = "monto_pagado", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal montoPagado = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @NotNull(message = "El estado es obligatorio")
    @Builder.Default
    private EstadoTasa estado = EstadoTasa.ACTIVA;

    @Enumerated(EnumType.STRING)
    @Column(name = "periodo_facturacion", nullable = false, length = 20)
    @NotNull(message = "El período de facturación es obligatorio")
    private PeriodoFacturacion periodoFacturacion;

    @Column(name = "fecha_inicio", nullable = false)
    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    // Información de ubicación/inmueble
    @Column(length = 200)
    private String direccion;

    @Column(name = "zona_municipal", length = 100)
    private String zonaMunicipal;

    @Column(name = "area_inmueble", precision = 10, scale = 2)
    private BigDecimal areaInmueble;

    @Column(name = "valor_catastral", precision = 15, scale = 2)
    private BigDecimal valorCatastral;

    @Column(length = 1000)
    private String observaciones;

    // Campos de auditoría
    @Column(name = "usuario_registro", nullable = false, length = 100)
    @NotBlank(message = "El usuario de registro es obligatorio")
    private String usuarioRegistro;

    @Column(name = "fecha_registro", nullable = false)
    @Builder.Default
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "usuario_modificacion", length = 100)
    private String usuarioModificacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    // Método para calcular saldo pendiente
    public BigDecimal getSaldoPendiente() {
        return montoBase.subtract(montoPagado);
    }

    // Método para verificar si está completamente pagada
    public boolean isCompletamentePagada() {
        return montoPagado.compareTo(montoBase) >= 0;
    }

    // Método para verificar si está vencida
    public boolean isVencida() {
        return fechaVencimiento != null && 
               LocalDateTime.now().isAfter(fechaVencimiento) && 
               !isCompletamentePagada();
    }
}
