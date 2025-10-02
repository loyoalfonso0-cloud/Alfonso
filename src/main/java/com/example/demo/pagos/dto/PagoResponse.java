package com.example.demo.pagos.dto;

import com.example.demo.pagos.model.EstadoPago;
import com.example.demo.pagos.model.MetodoPago;
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
public class PagoResponse {

    private Long id;
    private Long contribuyenteId;
    private String contribuyenteNombre;
    private String contribuyenteRif;
    private Long declaracionId;
    private Long multaId;
    private BigDecimal monto;
    private MetodoPago metodoPago;
    private String metodoPagoDescripcion;
    private String referencia;
    private EstadoPago estado;
    private String estadoDescripcion;
    private LocalDateTime fechaPago;
    private String concepto;
    private String comprobantePath;
    private LocalDateTime creadoEn;
    private String usuarioRegistro;

    // Getters y setters manuales para compatibilidad
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getContribuyenteId() { return contribuyenteId; }
    public void setContribuyenteId(Long contribuyenteId) { this.contribuyenteId = contribuyenteId; }

    public String getContribuyenteNombre() { return contribuyenteNombre; }
    public void setContribuyenteNombre(String contribuyenteNombre) { this.contribuyenteNombre = contribuyenteNombre; }

    public String getContribuyenteRif() { return contribuyenteRif; }
    public void setContribuyenteRif(String contribuyenteRif) { this.contribuyenteRif = contribuyenteRif; }

    public Long getDeclaracionId() { return declaracionId; }
    public void setDeclaracionId(Long declaracionId) { this.declaracionId = declaracionId; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public String getMetodoPagoDescripcion() { return metodoPagoDescripcion; }
    public void setMetodoPagoDescripcion(String metodoPagoDescripcion) { this.metodoPagoDescripcion = metodoPagoDescripcion; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public EstadoPago getEstado() { return estado; }
    public void setEstado(EstadoPago estado) { this.estado = estado; }

    public String getEstadoDescripcion() { return estadoDescripcion; }
    public void setEstadoDescripcion(String estadoDescripcion) { this.estadoDescripcion = estadoDescripcion; }

    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public String getComprobantePath() { return comprobantePath; }
    public void setComprobantePath(String comprobantePath) { this.comprobantePath = comprobantePath; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public String getUsuarioRegistro() { return usuarioRegistro; }
    public void setUsuarioRegistro(String usuarioRegistro) { this.usuarioRegistro = usuarioRegistro; }
}
