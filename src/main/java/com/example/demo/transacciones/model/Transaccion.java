package com.example.demo.transacciones.model;

import com.example.demo.tributario.model.Contribuyente;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacciones")
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_transaccion", unique = true, nullable = false)
    private String numeroTransaccion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_transaccion", nullable = false)
    private TipoTransaccion tipoTransaccion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoTransaccion estado = EstadoTransaccion.PENDIENTE;

    @NotNull
    @Positive
    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Size(max = 500)
    @Column(name = "concepto")
    private String concepto;

    @Size(max = 100)
    @Column(name = "referencia_externa")
    private String referenciaExterna;

    @Size(max = 100)
    @Column(name = "numero_comprobante")
    private String numeroComprobante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id")
    private Contribuyente contribuyente;

    @Column(name = "entidad_relacionada_tipo")
    private String entidadRelacionadaTipo; // "SERVICIO", "IMPUESTO", "TASA", "MULTA", etc.

    @Column(name = "entidad_relacionada_id")
    private Long entidadRelacionadaId;

    @Column(name = "fecha_transaccion", nullable = false)
    private LocalDateTime fechaTransaccion = LocalDateTime.now();

    @Column(name = "fecha_procesamiento")
    private LocalDateTime fechaProcesamiento;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @Size(max = 1000)
    @Column(name = "observaciones")
    private String observaciones;

    @Size(max = 100)
    @Column(name = "usuario_registro")
    private String usuarioRegistro;

    @Size(max = 100)
    @Column(name = "usuario_procesamiento")
    private String usuarioProcesamiento;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    // Constructors
    public Transaccion() {}

    public Transaccion(TipoTransaccion tipoTransaccion, BigDecimal monto, String concepto) {
        this.tipoTransaccion = tipoTransaccion;
        this.monto = monto;
        this.concepto = concepto;
        this.fechaTransaccion = LocalDateTime.now();
        this.fechaRegistro = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroTransaccion() {
        return numeroTransaccion;
    }

    public void setNumeroTransaccion(String numeroTransaccion) {
        this.numeroTransaccion = numeroTransaccion;
    }

    public TipoTransaccion getTipoTransaccion() {
        return tipoTransaccion;
    }

    public void setTipoTransaccion(TipoTransaccion tipoTransaccion) {
        this.tipoTransaccion = tipoTransaccion;
    }

    public EstadoTransaccion getEstado() {
        return estado;
    }

    public void setEstado(EstadoTransaccion estado) {
        this.estado = estado;
        if (estado == EstadoTransaccion.PROCESADA && this.fechaProcesamiento == null) {
            this.fechaProcesamiento = LocalDateTime.now();
        }
        if (estado == EstadoTransaccion.CONFIRMADA && this.fechaConfirmacion == null) {
            this.fechaConfirmacion = LocalDateTime.now();
        }
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public String getReferenciaExterna() {
        return referenciaExterna;
    }

    public void setReferenciaExterna(String referenciaExterna) {
        this.referenciaExterna = referenciaExterna;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(String numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    public Contribuyente getContribuyente() {
        return contribuyente;
    }

    public void setContribuyente(Contribuyente contribuyente) {
        this.contribuyente = contribuyente;
    }

    public String getEntidadRelacionadaTipo() {
        return entidadRelacionadaTipo;
    }

    public void setEntidadRelacionadaTipo(String entidadRelacionadaTipo) {
        this.entidadRelacionadaTipo = entidadRelacionadaTipo;
    }

    public Long getEntidadRelacionadaId() {
        return entidadRelacionadaId;
    }

    public void setEntidadRelacionadaId(Long entidadRelacionadaId) {
        this.entidadRelacionadaId = entidadRelacionadaId;
    }

    public LocalDateTime getFechaTransaccion() {
        return fechaTransaccion;
    }

    public void setFechaTransaccion(LocalDateTime fechaTransaccion) {
        this.fechaTransaccion = fechaTransaccion;
    }

    public LocalDateTime getFechaProcesamiento() {
        return fechaProcesamiento;
    }

    public void setFechaProcesamiento(LocalDateTime fechaProcesamiento) {
        this.fechaProcesamiento = fechaProcesamiento;
    }

    public LocalDateTime getFechaConfirmacion() {
        return fechaConfirmacion;
    }

    public void setFechaConfirmacion(LocalDateTime fechaConfirmacion) {
        this.fechaConfirmacion = fechaConfirmacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getUsuarioRegistro() {
        return usuarioRegistro;
    }

    public void setUsuarioRegistro(String usuarioRegistro) {
        this.usuarioRegistro = usuarioRegistro;
    }

    public String getUsuarioProcesamiento() {
        return usuarioProcesamiento;
    }

    public void setUsuarioProcesamiento(String usuarioProcesamiento) {
        this.usuarioProcesamiento = usuarioProcesamiento;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    // Business methods
    public boolean isPendiente() {
        return EstadoTransaccion.PENDIENTE.equals(this.estado);
    }

    public boolean isProcesada() {
        return EstadoTransaccion.PROCESADA.equals(this.estado) || 
               EstadoTransaccion.CONFIRMADA.equals(this.estado);
    }

    public boolean isAnulada() {
        return EstadoTransaccion.ANULADA.equals(this.estado);
    }

    public void procesar(String usuario) {
        if (this.isPendiente()) {
            this.setEstado(EstadoTransaccion.PROCESADA);
            this.setUsuarioProcesamiento(usuario);
            this.setFechaProcesamiento(LocalDateTime.now());
        }
    }

    public void confirmar() {
        if (EstadoTransaccion.PROCESADA.equals(this.estado)) {
            this.setEstado(EstadoTransaccion.CONFIRMADA);
            this.setFechaConfirmacion(LocalDateTime.now());
        }
    }

    public void anular(String motivo) {
        if (!this.isAnulada()) {
            this.setEstado(EstadoTransaccion.ANULADA);
            this.setObservaciones((this.observaciones != null ? this.observaciones + " | " : "") + 
                                "Anulada: " + motivo);
            this.setFechaModificacion(LocalDateTime.now());
        }
    }
}
