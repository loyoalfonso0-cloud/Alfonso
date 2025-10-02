package com.example.demo.transacciones.dto;

import com.example.demo.transacciones.model.EstadoTransaccion;
import com.example.demo.transacciones.model.TipoTransaccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransaccionResponse {

    private Long id;
    private String numeroTransaccion;
    private TipoTransaccion tipoTransaccion;
    private String tipoTransaccionDescripcion;
    private EstadoTransaccion estado;
    private String estadoDescripcion;
    private BigDecimal monto;
    private String concepto;
    private String referenciaExterna;
    private String numeroComprobante;
    private Long contribuyenteId;
    private String contribuyenteNombre;
    private String contribuyenteRif;
    private String entidadRelacionadaTipo;
    private Long entidadRelacionadaId;
    private LocalDateTime fechaTransaccion;
    private LocalDateTime fechaProcesamiento;
    private LocalDateTime fechaConfirmacion;
    private String observaciones;
    private String usuarioRegistro;
    private String usuarioProcesamiento;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaModificacion;

    // Constructors
    public TransaccionResponse() {}

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
        this.tipoTransaccionDescripcion = tipoTransaccion != null ? tipoTransaccion.getDescripcion() : null;
    }

    public String getTipoTransaccionDescripcion() {
        return tipoTransaccionDescripcion;
    }

    public void setTipoTransaccionDescripcion(String tipoTransaccionDescripcion) {
        this.tipoTransaccionDescripcion = tipoTransaccionDescripcion;
    }

    public EstadoTransaccion getEstado() {
        return estado;
    }

    public void setEstado(EstadoTransaccion estado) {
        this.estado = estado;
        this.estadoDescripcion = estado != null ? estado.getDescripcion() : null;
    }

    public String getEstadoDescripcion() {
        return estadoDescripcion;
    }

    public void setEstadoDescripcion(String estadoDescripcion) {
        this.estadoDescripcion = estadoDescripcion;
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

    public Long getContribuyenteId() {
        return contribuyenteId;
    }

    public void setContribuyenteId(Long contribuyenteId) {
        this.contribuyenteId = contribuyenteId;
    }

    public String getContribuyenteNombre() {
        return contribuyenteNombre;
    }

    public void setContribuyenteNombre(String contribuyenteNombre) {
        this.contribuyenteNombre = contribuyenteNombre;
    }

    public String getContribuyenteRif() {
        return contribuyenteRif;
    }

    public void setContribuyenteRif(String contribuyenteRif) {
        this.contribuyenteRif = contribuyenteRif;
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
}
