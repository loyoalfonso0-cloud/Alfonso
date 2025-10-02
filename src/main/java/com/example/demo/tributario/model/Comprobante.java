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
@Table(name = "comprobantes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comprobante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El contribuyente es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)  // Cambiado de LAZY a EAGER
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private Contribuyente contribuyente;

    @NotBlank(message = "El tipo es obligatorio")
    @Column(nullable = false, length = 40)
    private String tipo;

    @NotBlank(message = "La serie es obligatoria")
    @Column(nullable = false, length = 10)
    private String serie;

    @NotBlank(message = "El número es obligatorio")
    @Column(nullable = false, length = 20)
    private String numero;

    @NotNull(message = "La fecha de emisión es obligatoria")
    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @NotNull(message = "El subtotal es obligatorio")
    @Positive(message = "El subtotal debe ser positivo")
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal subtotal;

    @NotNull(message = "El impuesto es obligatorio")
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal impuesto;

    @NotNull(message = "El total es obligatorio")
    @Positive(message = "El total debe ser positivo")
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoComprobante estado = EstadoComprobante.EMITIDO;

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
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }
    
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    
    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }
    
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    
    public BigDecimal getImpuesto() { return impuesto; }
    public void setImpuesto(BigDecimal impuesto) { this.impuesto = impuesto; }
    
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    
    public EstadoComprobante getEstado() { return estado; }
    public void setEstado(EstadoComprobante estado) { this.estado = estado; }
    
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
    
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }

    public enum TipoComprobante {
        FACTURA("Factura"),
        BOLETA("Boleta"),
        NOTA_CREDITO("Nota de Crédito"),
        NOTA_DEBITO("Nota de Débito"),
        RECIBO("Recibo");

        private final String descripcion;

        TipoComprobante(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    public enum EstadoComprobante {
        EMITIDO("Emitido"),
        PAGADO("Pagado"),
        ANULADO("Anulado"),
        VENCIDO("Vencido");

        private final String descripcion;

        EstadoComprobante(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }
}
