package com.example.demo.auditoria.listener;

import com.example.demo.auditoria.service.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// @Component - DESHABILITADO: No registrar logins/logouts
@RequiredArgsConstructor
@Slf4j
public class AuthenticationEventListener {
    
    private final AuditoriaService auditoriaService;
    
    /**
     * Captura eventos de login exitoso - DESHABILITADO
     */
    // @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        try {
            Authentication authentication = event.getAuthentication();
            String username = authentication.getName();
            String ipAddress = obtenerIpAddress();
            
            auditoriaService.registrarLogin(username, ipAddress);
            
            log.info("Login exitoso registrado en auditoría: usuario={}, ip={}", username, ipAddress);
            
        } catch (Exception e) {
            log.error("Error al registrar login en auditoría: {}", e.getMessage());
        }
    }
    
    /**
     * Captura eventos de logout - DESHABILITADO
     */
    // @EventListener
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        try {
            Authentication authentication = event.getAuthentication();
            if (authentication != null) {
                String username = authentication.getName();
                auditoriaService.registrarLogout(username);
                
                log.info("Logout registrado en auditoría: usuario={}", username);
            }
        } catch (Exception e) {
            log.error("Error al registrar logout en auditoría: {}", e.getMessage());
        }
    }
    
    /**
     * Obtiene la dirección IP del request actual
     */
    private String obtenerIpAddress() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            if (attr != null) {
                HttpServletRequest request = attr.getRequest();
                return obtenerIpReal(request);
            }
        } catch (Exception e) {
            log.debug("No se pudo obtener IP del request: {}", e.getMessage());
        }
        return "unknown";
    }
    
    /**
     * Obtiene la IP real considerando proxies
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
        
        return ip != null ? ip : "unknown";
    }
}
