package com.example.demo.pagos.dto;

import com.example.demo.pagos.model.MetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class PagoRequest {

    private Long contribuyenteId;

    private Long declaracionId;
    
    private Long multaId; // Opcional

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;
    private MetodoPago metodoPago;

    private String referencia;

    private LocalDateTime fechaPago;

    @NotBlank(message = "El concepto es obligatorio")
    private String concepto;

    private String usuarioRegistro;

    // Getters y setters manuales para compatibilidad
    public Long getContribuyenteId() { return contribuyenteId; }
    public void setContribuyenteId(Long contribuyenteId) { this.contribuyenteId = contribuyenteId; }

    public Long getDeclaracionId() { return declaracionId; }
    public void setDeclaracionId(Long declaracionId) { this.declaracionId = declaracionId; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public String getUsuarioRegistro() { return usuarioRegistro; }
    public void setUsuarioRegistro(String usuarioRegistro) { this.usuarioRegistro = usuarioRegistro; }
}
