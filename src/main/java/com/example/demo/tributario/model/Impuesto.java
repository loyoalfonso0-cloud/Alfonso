package com.example.demo.tributario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "impuestos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Impuesto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal tasa = BigDecimal.ZERO;
}


