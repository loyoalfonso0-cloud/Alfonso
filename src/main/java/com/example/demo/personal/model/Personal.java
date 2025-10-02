package com.example.demo.personal.model;

import com.example.demo.security.model.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "personal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Personal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = true)
    @JoinColumn(name = "usuario_id", nullable = true)
    private Usuario usuario;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String nombres;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String apellidos;

    @Column(length = 10)
    private String tipoDocumento; // CC, CE, PASAPORTE

    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String documento;

    @Email
    @Column(length = 150)
    private String email;

    @Column(length = 30)
    private String telefono;

    @Column(length = 200)
    private String direccion;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String cargo;

    @Column(length = 50)
    private String departamento; // TRIBUTARIO, ADMINISTRACION, etc.

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @Column(precision = 12, scale = 2)
    private BigDecimal salario;

    @Column(length = 500)
    private String observaciones;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;
}


