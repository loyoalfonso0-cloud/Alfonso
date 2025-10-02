package com.example.demo.servicios.domain;

import com.example.demo.tributario.model.Contribuyente;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa un Servicio Municipal
 */
@Entity
@Table(name = "servicios")
@Data
@EqualsAndHashCode(exclude = {"contribuyente"})
@ToString(exclude = {"contribuyente"})
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_servicio", unique = true, nullable = false, length = 20)
    private String numeroServicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private Contribuyente contribuyente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_servicio", nullable = false)
    private TipoServicio tipoServicio;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoServicio estado = EstadoServicio.ACTIVO;

    @Column(name = "tarifa_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifaBase;

    @Column(name = "consumo_actual", precision = 10, scale = 2)
    private BigDecimal consumoActual = BigDecimal.ZERO;

    @Column(name = "consumo_anterior", precision = 10, scale = 2)
    private BigDecimal consumoAnterior = BigDecimal.ZERO;

    @Column(name = "monto_facturado", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoFacturado = BigDecimal.ZERO;

    @Column(name = "monto_pagado", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoPagado = BigDecimal.ZERO;

    @Column(name = "fecha_instalacion")
    private LocalDate fechaInstalacion;

    @Column(name = "fecha_ultima_lectura")
    private LocalDate fechaUltimaLectura;

    @Column(name = "fecha_proxima_lectura")
    private LocalDate fechaProximaLectura;

    @Column(name = "fecha_corte")
    private LocalDate fechaCorte;

    @Column(name = "direccion_servicio", length = 500)
    private String direccionServicio;

    @Column(name = "medidor", length = 50)
    private String medidor;

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    // Campos de auditoría
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "usuario_registro", length = 100)
    private String usuarioRegistro;

    @Column(name = "usuario_modificacion", length = 100)
    private String usuarioModificacion;

    /**
     * Calcula el saldo pendiente del servicio
     */
    public BigDecimal getSaldoPendiente() {
        return montoFacturado.subtract(montoPagado);
    }

    /**
     * Calcula el consumo del período actual
     */
    public BigDecimal getConsumoDelPeriodo() {
        if (consumoActual != null && consumoAnterior != null) {
            return consumoActual.subtract(consumoAnterior);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Verifica si el servicio tiene deuda pendiente
     */
    public boolean tieneDeudaPendiente() {
        return getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Verifica si el servicio está vencido para corte
     */
    public boolean estaVencidoParaCorte() {
        return fechaCorte != null && LocalDate.now().isAfter(fechaCorte) && tieneDeudaPendiente();
    }

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        if (numeroServicio == null) {
            generarNumeroServicio();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }

    /**
     * Genera un número único para el servicio
     */
    private void generarNumeroServicio() {
        String year = String.valueOf(LocalDate.now().getYear());
        String prefix = "SRV-" + year + "-";
        // El número completo se generará en el servicio con secuencia
        this.numeroServicio = prefix;
    }
}
