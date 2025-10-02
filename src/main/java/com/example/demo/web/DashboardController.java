package com.example.demo.web;

import com.example.demo.auditoria.repository.AuditoriaLogRepository;
import com.example.demo.personal.repository.PersonalRepository;
import com.example.demo.security.repository.RolRepository;
import com.example.demo.tributario.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class DashboardController {
    
    @Autowired
    private ContribuyenteRepository contribuyenteRepository;
    
    @Autowired
    private ComprobanteRepository comprobanteRepository;
    
    @Autowired
    private ImpuestoRepository impuestoRepository;
    
    @Autowired
    private RetencionRepository retencionRepository;
    
    @Autowired
    private PersonalRepository personalRepository;
    
    @Autowired
    private RolRepository rolRepository;
    
    @Autowired
    private AuditoriaLogRepository auditoriaLogRepository;
    
    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model, Authentication authentication) {
        model.addAttribute("titulo", "Panel Tributario");
        
        // Obtener permisos del usuario actual
        Set<String> userAuthorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        
        // Determinar qué módulos puede acceder el usuario
        Map<String, Boolean> permisos = new HashMap<>();
        permisos.put("contribuyentes", hasContribuyentesAccess(userAuthorities));
        permisos.put("impuestos", hasImpuestosAccess(userAuthorities));
        permisos.put("retenciones", hasRetencionesAccess(userAuthorities));
        permisos.put("comprobantes", hasComprobantesAccess(userAuthorities));
        permisos.put("declaraciones", hasDeclaracionesAccess(userAuthorities));
        permisos.put("roles", hasRolesAccess(userAuthorities));
        permisos.put("personal", hasPersonalAccess(userAuthorities));
        permisos.put("auditoria", hasAuditoriaAccess(userAuthorities));
        
        model.addAttribute("permisos", permisos);
        
        // Estadísticas del sistema con datos reales
        Map<String, Object> estadisticas = new HashMap<>();
        
        try {
            // Obtener conteos reales de la base de datos
            long totalContribuyentes = contribuyenteRepository.count();
            long totalComprobantes = comprobanteRepository.count();
            long totalImpuestos = impuestoRepository.count();
            long totalRetenciones = retencionRepository.count();
            long totalPersonal = personalRepository.count();
            long totalRoles = rolRepository.count();
            long totalLogs = auditoriaLogRepository.count();
            
            estadisticas.put("totalContribuyentes", totalContribuyentes);
            estadisticas.put("totalComprobantes", totalComprobantes);
            estadisticas.put("totalImpuestos", totalImpuestos);
            estadisticas.put("totalRetenciones", totalRetenciones);
            estadisticas.put("totalPersonal", totalPersonal);
            estadisticas.put("totalRoles", totalRoles);
            estadisticas.put("totalLogs", totalLogs);
            
            // Estadísticas adicionales
            estadisticas.put("totalRegistros", totalContribuyentes + totalComprobantes + totalImpuestos + totalRetenciones);
            
        } catch (Exception e) {
            // Valores por defecto en caso de error
            System.err.println("Error al obtener estadísticas: " + e.getMessage());
            estadisticas.put("totalContribuyentes", 0L);
            estadisticas.put("totalComprobantes", 0L);
            estadisticas.put("totalImpuestos", 0L);
            estadisticas.put("totalRetenciones", 0L);
            estadisticas.put("totalPersonal", 0L);
            estadisticas.put("totalRoles", 0L);
            estadisticas.put("totalLogs", 0L);
            estadisticas.put("totalRegistros", 0L);
        }
        
        model.addAttribute("estadisticas", estadisticas);
        
        return "dashboard/index";
    }
    
    // Métodos auxiliares para verificar permisos
    private boolean hasContribuyentesAccess(Set<String> authorities) {
        return authorities.contains("ROLE_ADMIN_TRIBUTARIO") ||
               authorities.contains("CONTRIBUYENTES_READ") ||
               authorities.contains("CONTRIBUYENTES_VER") ||
               authorities.contains("CONTRIBUYENTES_GESTIONAR");
    }
    
    private boolean hasImpuestosAccess(Set<String> authorities) {
        return authorities.contains("ROLE_ADMIN_TRIBUTARIO") ||
               authorities.contains("IMPUESTOS_READ") ||
               authorities.contains("IMPUESTOS_VER") ||
               authorities.contains("IMPUESTOS_GESTIONAR");
    }
    
    private boolean hasRetencionesAccess(Set<String> authorities) {
        return authorities.contains("ROLE_ADMIN_TRIBUTARIO") ||
               authorities.contains("RETENCIONES_READ") ||
               authorities.contains("RETENCIONES_VER") ||
               authorities.contains("RETENCIONES_GESTIONAR");
    }
    
    private boolean hasComprobantesAccess(Set<String> authorities) {
        return authorities.contains("ROLE_ADMIN_TRIBUTARIO") ||
               authorities.contains("COMPROBANTES_READ") ||
               authorities.contains("COMPROBANTES_VER") ||
               authorities.contains("COMPROBANTES_GESTIONAR");
    }
    
    private boolean hasDeclaracionesAccess(Set<String> authorities) {
        return authorities.contains("ROLE_ADMIN_TRIBUTARIO") ||
               authorities.contains("DECLARACIONES_READ") ||
               authorities.contains("DECLARACIONES_VER") ||
               authorities.contains("DECLARACIONES_GESTIONAR");
    }
    
    private boolean hasRolesAccess(Set<String> authorities) {
        return authorities.contains("ROLE_ADMIN_TRIBUTARIO") ||
               authorities.contains("ROLES_READ") ||
               authorities.contains("ROLES_VER") ||
               authorities.contains("ROLES_GESTIONAR");
    }
    
    private boolean hasPersonalAccess(Set<String> authorities) {
        return authorities.contains("ROLE_ADMIN_TRIBUTARIO") ||
               authorities.contains("USUARIOS_READ") ||
               authorities.contains("USUARIOS_VER") ||
               authorities.contains("USUARIOS_CREAR") ||
               authorities.contains("USUARIOS_EDITAR") ||
               authorities.contains("USUARIOS_ELIMINAR") ||
               authorities.contains("PERSONAL_READ") ||
               authorities.contains("PERSONAL_VER") ||
               authorities.contains("PERSONAL_GESTIONAR");
    }
    
    private boolean hasAuditoriaAccess(Set<String> authorities) {
        return authorities.contains("ROLE_SUPER_ADMIN") ||
               authorities.contains("AUDITORIA_VER") ||
               authorities.contains("AUDITORIA_GESTIONAR") ||
               authorities.contains("AUDITORIA_EXPORTAR") ||
               authorities.contains("AUDITORIA_ESTADISTICAS");
    }
    
}


