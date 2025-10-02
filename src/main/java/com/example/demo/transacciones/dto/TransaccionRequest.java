package com.example.demo.transacciones.dto;

import com.example.demo.transacciones.model.TipoTransaccion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransaccionRequest {

    @NotNull(message = "El tipo de transacción es obligatorio")
    private TipoTransaccion tipoTransaccion;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser positivo")
    private BigDecimal monto;

    @Size(max = 500, message = "El concepto no puede exceder 500 caracteres")
    private String concepto;

    @Size(max = 100, message = "La referencia externa no puede exceder 100 caracteres")
    private String referenciaExterna;

    @Size(max = 100, message = "El número de comprobante no puede exceder 100 caracteres")
    private String numeroComprobante;

    private Long contribuyenteId;

    @Size(max = 50, message = "El tipo de entidad relacionada no puede exceder 50 caracteres")
    private String entidadRelacionadaTipo;

    private Long entidadRelacionadaId;

    private LocalDateTime fechaTransaccion;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;

    // Constructors
    public TransaccionRequest() {}

    public TransaccionRequest(TipoTransaccion tipoTransaccion, BigDecimal monto, String concepto) {
        this.tipoTransaccion = tipoTransaccion;
        this.monto = monto;
        this.concepto = concepto;
    }

    // Getters and Setters
    public TipoTransaccion getTipoTransaccion() {
        return tipoTransaccion;
    }

    public void setTipoTransaccion(TipoTransaccion tipoTransaccion) {
        this.tipoTransaccion = tipoTransaccion;
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

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
