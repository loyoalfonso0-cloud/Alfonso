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
public class PagoResponseSimple {
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
}
