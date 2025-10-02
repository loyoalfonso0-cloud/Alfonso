package com.example.demo.servicios.dto;

import com.example.demo.servicios.domain.EstadoServicio;
import com.example.demo.servicios.domain.TipoServicio;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para respuestas de Servicio
 */
@Data
public class ServicioResponse {
    private Long id;
    private String numeroServicio;
    private Long contribuyenteId;
    private String contribuyenteNombre;
    private String contribuyenteRif;
    private TipoServicio tipoServicio;
    private String tipoServicioDescripcion;
    private EstadoServicio estado;
    private String estadoDescripcion;
    private String estadoColorClass;
    private BigDecimal tarifaBase;
    private BigDecimal consumoActual;
    private BigDecimal consumoAnterior;
    private BigDecimal consumoDelPeriodo;
    private BigDecimal montoFacturado;
    private BigDecimal montoPagado;
    private BigDecimal saldoPendiente;
    private LocalDate fechaInstalacion;
    private LocalDate fechaUltimaLectura;
    private LocalDate fechaProximaLectura;
    private LocalDate fechaCorte;
    private String direccionServicio;
    private String medidor;
    private String observaciones;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaModificacion;
    private String usuarioRegistro;
    private String usuarioModificacion;
    
    // Campos calculados
    private boolean tieneDeudaPendiente;
    private boolean estaVencidoParaCorte;
    private boolean requiereMedidor;
    private boolean esFacturablePorConsumo;
    private boolean esOperativo;
    private boolean permiteFacturacion;
}
