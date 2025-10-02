package com.example.demo.tributario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "retenciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Retencion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El contribuyente es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)  // Cambiado de LAZY a EAGER
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private Contribuyente contribuyente;

    @NotNull(message = "La fecha es obligatoria")
    @Column(nullable = false)
    private LocalDate fecha;

    @NotBlank(message = "El concepto es obligatorio")
    @Column(nullable = false, length = 200)
    private String concepto;

    @NotNull(message = "El porcentaje es obligatorio")
    @Positive(message = "El porcentaje debe ser positivo")
    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal porcentaje;

    @NotNull(message = "El monto base es obligatorio")
    @Positive(message = "El monto base debe ser positivo")
    @Column(name = "monto_base", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoBase;

    @Column(name = "monto_retenido", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoRetenido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoRetencion estado = EstadoRetencion.PENDIENTE;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column
    private LocalDateTime actualizadoEn;

    @PreUpdate
    public void preUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }

    // Métodos getter/setter manuales para asegurar compatibilidad
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Contribuyente getContribuyente() { return contribuyente; }
    public void setContribuyente(Contribuyente contribuyente) { this.contribuyente = contribuyente; }
    
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    
    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal porcentaje) { this.porcentaje = porcentaje; }
    
    public BigDecimal getMontoBase() { return montoBase; }
    public void setMontoBase(BigDecimal montoBase) { this.montoBase = montoBase; }
    
    public BigDecimal getMontoRetenido() { return montoRetenido; }
    public void setMontoRetenido(BigDecimal montoRetenido) { this.montoRetenido = montoRetenido; }
    
    public EstadoRetencion getEstado() { return estado; }
    public void setEstado(EstadoRetencion estado) { this.estado = estado; }
    
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
    
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }

    public enum EstadoRetencion {
        PENDIENTE("Pendiente"),
        APLICADA("Aplicada"),
        ANULADA("Anulada");

        private final String descripcion;

        EstadoRetencion(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }
}
