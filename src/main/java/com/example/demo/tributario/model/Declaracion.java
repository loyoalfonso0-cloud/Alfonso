package com.example.demo.tributario.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "declaraciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Declaracion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "contribuyente_id")
    private Contribuyente contribuyente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "impuesto_id")
    private Impuesto impuesto;

    @Column(nullable = false, length = 20)
    private String periodo;

    @Column(nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal baseImponible = BigDecimal.ZERO;

    @Column(nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal monto = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "PENDIENTE";

    @Column(name = "creado_en")
    @Builder.Default
    private Instant creadoEn = Instant.now();
}


