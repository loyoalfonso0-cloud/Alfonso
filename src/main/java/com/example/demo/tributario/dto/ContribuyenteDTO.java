package com.example.demo.tributario.dto;

import com.example.demo.tributario.model.Contribuyente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContribuyenteDTO {
    
    private Long id;
    
    @NotBlank(message = "El RIF es obligatorio")
    @Pattern(regexp = "[VJEGPRC]-\\d{8}-\\d", message = "El RIF debe tener formato V-12345678-9")
    private String rif;
    
    @NotBlank(message = "La razón social es obligatoria")
    private String razonSocial;
    
    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;
    
    private String telefono;
    
    @Email(message = "Formato de email inválido")
    @NotBlank(message = "El email es obligatorio")
    private String email;
    
    private Contribuyente.TipoContribuyente tipoContribuyente;
    
    private String representanteLegal;
    
    private Boolean activo;
    
    // Método para convertir de Entity a DTO
    public static ContribuyenteDTO fromEntity(Contribuyente contribuyente) {
        return ContribuyenteDTO.builder()
            .id(contribuyente.getId())
            .rif(contribuyente.getRif())
            .razonSocial(contribuyente.getRazonSocial())
            .direccion(contribuyente.getDireccion())
            .telefono(contribuyente.getTelefono())
            .email(contribuyente.getEmail())
            .tipoContribuyente(contribuyente.getTipoContribuyente())
            .representanteLegal(contribuyente.getRepresentanteLegal())
            .activo(contribuyente.getActivo())
            .build();
    }
    
    // Método para convertir de DTO a Entity
    public Contribuyente toEntity() {
        return Contribuyente.builder()
            .id(this.id)
            .rif(this.rif)
            .razonSocial(this.razonSocial)
            .direccion(this.direccion)
            .telefono(this.telefono)
            .email(this.email)
            .tipoContribuyente(this.tipoContribuyente != null ? this.tipoContribuyente : Contribuyente.TipoContribuyente.PERSONA_NATURAL)
            .representanteLegal(this.representanteLegal)
            .activo(this.activo != null ? this.activo : true)
            .build();
    }
}
