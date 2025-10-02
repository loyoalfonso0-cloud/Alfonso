package com.example.demo.auditoria.service;

import com.example.demo.auditoria.model.AuditoriaLog;
import com.example.demo.auditoria.repository.AuditoriaLogRepository;
import com.example.demo.personal.model.Personal;
import com.example.demo.personal.repository.PersonalRepository;
import com.example.demo.security.model.Usuario;
import com.example.demo.security.repository.UsuarioRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuditoriaService {
    
    private final AuditoriaLogRepository auditoriaLogRepository;
    private final ObjectMapper objectMapper;
    private final UsuarioRepository usuarioRepository;
    private final PersonalRepository personalRepository;
    
    // ==================== MÉTODOS PARA REGISTRAR LOGS ====================
    
    /**
     * Registra una acción en el sistema de auditoría
     */
    public void registrarAccion(String accion, String modulo, String descripcion) {
        try {
            String usuario = obtenerUsuarioActual();
            AuditoriaLog log = AuditoriaLog.crear(usuario, accion, modulo);
            log.setDescripcion(descripcion);
            log.setCedulaPersonal(obtenerCedulaPersonal(usuario));
            completarDatosDeRequest(log);
            auditoriaLogRepository.save(log);
        } catch (Exception e) {
            log.error("Error al registrar acción en auditoría: {}", e.getMessage());
        }
    }
    
    /**
     * Registra una acción con entidad específica
     */
    public void registrarAccionConEntidad(String accion, String modulo, String entidad, 
                                         Long entidadId, String descripcion) {
        try {
            String usuario = obtenerUsuarioActual();
            AuditoriaLog log = AuditoriaLog.crear(usuario, accion, modulo, entidad, entidadId, descripcion);
            log.setCedulaPersonal(obtenerCedulaPersonal(usuario));
            completarDatosDeRequest(log);
            auditoriaLogRepository.save(log);
        } catch (Exception e) {
            log.error("Error al registrar acción con entidad en auditoría: {}", e.getMessage());
        }
    }
    
    /**
     * Registra una actualización con valores anteriores y nuevos
     */
    public void registrarActualizacion(String modulo, String entidad, Long entidadId, 
                                     Object valoresAnteriores, Object valoresNuevos, String descripcion) {
        try {
            String usuario = obtenerUsuarioActual();
            AuditoriaLog log = AuditoriaLog.crear(usuario, "UPDATE", modulo, entidad, entidadId, descripcion);
            log.setCedulaPersonal(obtenerCedulaPersonal(usuario));
            
            // Convertir objetos a JSON
            if (valoresAnteriores != null) {
                log.setValoresAnteriores(objectMapper.writeValueAsString(valoresAnteriores));
            }
            if (valoresNuevos != null) {
                log.setValoresNuevos(objectMapper.writeValueAsString(valoresNuevos));
            }
            
            completarDatosDeRequest(log);
            auditoriaLogRepository.save(log);
        } catch (JsonProcessingException e) {
            log.error("Error al serializar valores para auditoría: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error al registrar actualización en auditoría: {}", e.getMessage());
        }
    }
    
    /**
     * Registra un login exitoso
     */
    public void registrarLogin(String usuario, String ipAddress) {
        try {
            AuditoriaLog log = AuditoriaLog.crear(usuario, "LOGIN", "SECURITY");
            log.setDescripcion("Usuario ingresó al sistema");
            log.setIpAddress(ipAddress);
            completarDatosDeRequest(log);
            auditoriaLogRepository.save(log);
        } catch (Exception e) {
            log.error("Error al registrar login en auditoría: {}", e.getMessage());
        }
    }
    
    /**
     * Registra un logout
     */
    public void registrarLogout(String usuario) {
        try {
            AuditoriaLog log = AuditoriaLog.crear(usuario, "LOGOUT", "SECURITY");
            log.setDescripcion("Usuario cerró sesión");
            completarDatosDeRequest(log);
            auditoriaLogRepository.save(log);
        } catch (Exception e) {
            log.error("Error al registrar logout en auditoría: {}", e.getMessage());
        }
    }
    
    /**
     * Registra un error en el sistema
     */
    public void registrarError(String accion, String modulo, String descripcion, String mensajeError) {
        try {
            String usuario = obtenerUsuarioActual();
            AuditoriaLog log = AuditoriaLog.crear(usuario, accion, modulo);
            log.setDescripcion(descripcion);
            log.setResultado("ERROR");
            log.setMensajeError(mensajeError);
            completarDatosDeRequest(log);
            auditoriaLogRepository.save(log);
        } catch (Exception e) {
            log.error("Error al registrar error en auditoría: {}", e.getMessage());
        }
    }
    
    // ==================== MÉTODOS DE CONSULTA ====================
    
    /**
     * Obtiene logs con filtros y paginación
     */
    @Transactional(readOnly = true)
    public Page<AuditoriaLog> obtenerLogsConFiltros(String usuario, String modulo, String accion,
                                                   LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                                   String ip, String termino, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditoriaLogRepository.buscarConFiltros(usuario, modulo, accion, fechaInicio, fechaFin, ip, termino, pageable);
    }
    
    /**
     * Obtiene todos los logs con paginación
     */
    @Transactional(readOnly = true)
    public Page<AuditoriaLog> obtenerTodosLosLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditoriaLogRepository.findAll(pageable);
    }
    
    /**
     * Obtiene estadísticas generales del sistema
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasGenerales() {
        Map<String, Object> estadisticas = new HashMap<>();
        LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);
        
        estadisticas.put("totalLogs", auditoriaLogRepository.contarTotalLogs());
        estadisticas.put("logsUltimos30Dias", auditoriaLogRepository.contarLogsRecientes(hace30Dias));
        estadisticas.put("usuariosActivosUltimos30Dias", auditoriaLogRepository.contarUsuariosActivos(hace30Dias));
        estadisticas.put("erroresUltimos30Dias", auditoriaLogRepository.contarErroresRecientes(hace30Dias));
        
        // Actividad por módulo
        estadisticas.put("actividadPorModulo", auditoriaLogRepository.obtenerActividadPorModulo(hace30Dias));
        
        // Actividad por usuario
        estadisticas.put("actividadPorUsuario", auditoriaLogRepository.obtenerActividadPorUsuario(hace30Dias));
        
        // Actividad por acción
        estadisticas.put("actividadPorAccion", auditoriaLogRepository.obtenerActividadPorAccion(hace30Dias));
        
        // Actividad por hora
        estadisticas.put("actividadPorHora", auditoriaLogRepository.obtenerActividadPorHora(hace30Dias));
        
        return estadisticas;
    }
    
    /**
     * Obtiene logs de errores recientes
     */
    @Transactional(readOnly = true)
    public List<AuditoriaLog> obtenerLogsDeErrores() {
        return auditoriaLogRepository.obtenerLogsDeErrores();
    }
    
    /**
     * Obtiene logs por usuario específico
     */
    public Page<AuditoriaLog> obtenerLogsPorUsuario(String usuario, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditoriaLogRepository.obtenerUltimosLogsPorUsuario(usuario, pageable);
    }
    
    /**
     * Obtiene un log específico por ID
     */
    public AuditoriaLog obtenerLogPorId(Long id) {
        return auditoriaLogRepository.findById(id).orElse(null);
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    /**
     * Obtiene el usuario actual del contexto de seguridad
     */
    private String obtenerUsuarioActual() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener el usuario actual: {}", e.getMessage());
        }
        return "SISTEMA";
    }
    
    /**
     * Obtiene la cédula del personal asociado al usuario
     */
    private String obtenerCedulaPersonal(String username) {
        try {
            // Buscar el usuario
            Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
            if (usuario != null) {
                // Buscar el personal asociado al usuario
                Personal personal = personalRepository.findByUsuarioId(usuario.getId()).orElse(null);
                if (personal != null) {
                    return personal.getDocumento(); // La cédula está en el campo documento
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener la cédula del personal para el usuario {}: {}", username, e.getMessage());
        }
        return null; // Si no se encuentra, devolver null
    }
    
    /**
     * Completa los datos de la request HTTP actual
     */
    private void completarDatosDeRequest(AuditoriaLog log) {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            if (attr != null) {
                HttpServletRequest request = attr.getRequest();
                
                // IP Address
                String ipAddress = obtenerIpReal(request);
                log.setIpAddress(ipAddress);
                
                // User Agent
                String userAgent = request.getHeader("User-Agent");
                if (userAgent != null && userAgent.length() > 500) {
                    userAgent = userAgent.substring(0, 500);
                }
                log.setUserAgent(userAgent);
                
                // Session ID
                if (request.getSession(false) != null) {
                    log.setSessionId(request.getSession().getId());
                }
            }
        } catch (Exception e) {
            // No se pudieron obtener datos de la request, continuar sin ellos
        }
    }
    
    /**
     * Obtiene la IP real del usuario considerando proxies
     */
    private String obtenerIpReal(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // Si hay múltiples IPs, tomar la primera
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
    
    /**
     * Convierte un objeto a JSON para almacenamiento
     */
    private String convertirAJson(Object objeto) {
        try {
            return objectMapper.writeValueAsString(objeto);
        } catch (JsonProcessingException e) {
            log.warn("Error al convertir objeto a JSON: {}", e.getMessage());
            return objeto.toString();
        }
    }
}
