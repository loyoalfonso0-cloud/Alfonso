package com.example.demo.multas.model;

import com.example.demo.tributario.model.Contribuyente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "multas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Multa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private Contribuyente contribuyente;
    
    @Column(name = "numero_multa", unique = true, nullable = false)
    private String numeroMulta;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_infraccion", nullable = false)
    private TipoInfraccion tipoInfraccion;
    
    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;
    
    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;
    
    @Column(name = "monto_pagado", precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0")
    private BigDecimal montoPagado = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoMulta estado;
    
    @Column(name = "fecha_infraccion", nullable = false)
    private LocalDateTime fechaInfraccion;
    
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDateTime fechaVencimiento;
    
    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;
    
    @Column(name = "observaciones", length = 1000)
    private String observaciones;
    
    @Column(name = "usuario_registro", nullable = false)
    private String usuarioRegistro;
    
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
    
    @Column(name = "usuario_modificacion")
    private String usuarioModificacion;
    
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;
    
    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoMulta.PENDIENTE;
        }
        if (montoPagado == null) {
            montoPagado = BigDecimal.ZERO;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
    
    // Métodos de negocio
    public BigDecimal getSaldoPendiente() {
        return monto.subtract(montoPagado);
    }
    
    public boolean estaPagada() {
        return montoPagado.compareTo(monto) >= 0;
    }
    
    public boolean estaVencida() {
        return LocalDateTime.now().isAfter(fechaVencimiento) && !estaPagada();
    }
    
    public void marcarComoPagada() {
        this.estado = EstadoMulta.PAGADA;
        this.fechaPago = LocalDateTime.now();
        this.montoPagado = this.monto;
    }
}
