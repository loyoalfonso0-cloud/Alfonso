package com.example.demo.tributario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "contribuyentes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contribuyente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El RIF es obligatorio")
    @Column(nullable = false, unique = true, length = 20)
    private String rif;

    @Column(length = 200)
    private String razonSocial;
    
    @Column(length = 100)
    private String nombre;
    
    @Column(length = 100)
    private String apellido;

    @NotBlank(message = "La dirección es obligatoria")
    @Column(nullable = false, length = 255)
    private String direccion;

    @Column(length = 40)
    private String telefono;

    @Email(message = "Formato de email inválido")
    @NotBlank(message = "El email es obligatorio")
    @Column(nullable = false, length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TipoContribuyente tipoContribuyente = TipoContribuyente.PERSONA_NATURAL;

    @Column(length = 200)
    private String representanteLegal;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime creadoEn = LocalDateTime.now();
    
    @PrePersist
    public void prePersist() {
        if (this.creadoEn == null) {
            this.creadoEn = LocalDateTime.now();
        }
        if (this.activo == null) {
            this.activo = true;
        }
    }

    @Column
    private LocalDateTime actualizadoEn;

    @PreUpdate
    public void preUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }

    // Métodos getter/setter manuales para asegurar compatibilidad
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getRif() { return rif; }
    public void setRif(String rif) { this.rif = rif; }
    
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public TipoContribuyente getTipoContribuyente() { return tipoContribuyente; }
    public void setTipoContribuyente(TipoContribuyente tipoContribuyente) { this.tipoContribuyente = tipoContribuyente; }
    
    public String getRepresentanteLegal() { return representanteLegal; }
    public void setRepresentanteLegal(String representanteLegal) { this.representanteLegal = representanteLegal; }
    
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
    
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }

    public enum TipoContribuyente {
        PERSONA_NATURAL("Persona Natural"),
        PERSONA_JURIDICA("Persona Jurídica");

        private final String descripcion;

        TipoContribuyente(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }
}


