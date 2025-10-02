package com.example.demo.auditoria.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "usuario", nullable = false, length = 100)
    private String usuario;
    
    @Column(name = "cedula_personal", length = 20)
    private String cedulaPersonal; // Cédula del personal que realiza la acción
    
    @Column(name = "accion", nullable = false, length = 50)
    private String accion; // CREATE, UPDATE, DELETE, LOGIN, LOGOUT, EXPORT, etc.
    
    @Column(name = "modulo", nullable = false, length = 50)
    private String modulo; // CONTRIBUYENTES, RETENCIONES, COMPROBANTES, etc.
    
    @Column(name = "entidad", length = 50)
    private String entidad; // Nombre de la entidad afectada
    
    @Column(name = "entidad_id")
    private Long entidadId; // ID del registro afectado
    
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion; // Descripción detallada de la acción
    
    @Column(name = "valores_anteriores", columnDefinition = "TEXT")
    private String valoresAnteriores; // JSON con valores antes del cambio
    
    @Column(name = "valores_nuevos", columnDefinition = "TEXT")
    private String valoresNuevos; // JSON con valores después del cambio
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress; // Dirección IP del usuario (IPv4 o IPv6)
    
    @Column(name = "user_agent", length = 500)
    private String userAgent; // Información del navegador/cliente
    
    @Column(name = "session_id", length = 100)
    private String sessionId; // ID de la sesión
    
    @CreationTimestamp
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;
    
    @Column(name = "resultado", length = 20)
    private String resultado; // SUCCESS, ERROR, UNAUTHORIZED, etc.
    
    @Column(name = "mensaje_error", columnDefinition = "TEXT")
    private String mensajeError; // Mensaje de error si la acción falló
    
    // Método estático para crear logs fácilmente
    public static AuditoriaLog crear(String usuario, String accion, String modulo) {
        return AuditoriaLog.builder()
                .usuario(usuario)
                .accion(accion)
                .modulo(modulo)
                .resultado("SUCCESS")
                .build();
    }
    
    // Método para crear log con entidad
    public static AuditoriaLog crear(String usuario, String accion, String modulo, 
                                   String entidad, Long entidadId, String descripcion) {
        return AuditoriaLog.builder()
                .usuario(usuario)
                .accion(accion)
                .modulo(modulo)
                .entidad(entidad)
                .entidadId(entidadId)
                .descripcion(descripcion)
                .resultado("SUCCESS")
                .build();
    }
}
