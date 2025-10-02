package com.example.demo.auditoria.aspect;

import com.example.demo.auditoria.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditoriaAspect {
    
    private final AuditoriaService auditoriaService;
    
    // ==================== POINTCUTS ====================
    
    /**
     * Captura todos los métodos de controladores
     */
    @Pointcut("within(@org.springframework.stereotype.Controller *) || " +
              "within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}
    
    /**
     * Captura métodos de servicios
     */
    @Pointcut("within(com.example.demo.tributario.service.*) || " +
              "within(com.example.demo.personal.service.*) || " +
              "within(com.example.demo.security.service.*)")
    public void serviceMethods() {}
    
    /**
     * Captura operaciones POST (CREATE)
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMappingMethods() {}
    
    /**
     * Captura operaciones PUT (UPDATE)
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void putMappingMethods() {}
    
    /**
     * Captura operaciones DELETE
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void deleteMappingMethods() {}
    
    /**
     * Captura operaciones GET (READ)
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public void getMappingMethods() {}
    
    // ==================== ADVICE METHODS ====================
    
    /**
     * Intercepta operaciones CREATE (POST)
     */
    @Around("controllerMethods() && postMappingMethods()")
    public Object auditarCreacion(ProceedingJoinPoint joinPoint) throws Throwable {
        return ejecutarConAuditoria(joinPoint, "CREATE");
    }
    
    /**
     * Intercepta operaciones UPDATE (PUT)
     */
    @Around("controllerMethods() && putMappingMethods()")
    public Object auditarActualizacion(ProceedingJoinPoint joinPoint) throws Throwable {
        return ejecutarConAuditoria(joinPoint, "UPDATE");
    }
    
    /**
     * Intercepta operaciones DELETE
     */
    @Around("controllerMethods() && deleteMappingMethods()")
    public Object auditarEliminacion(ProceedingJoinPoint joinPoint) throws Throwable {
        return ejecutarConAuditoria(joinPoint, "DELETE");
    }
    
    /**
     * Intercepta solo operaciones de exportación - NO navegación ni vistas
     */
    @Around("controllerMethods() && getMappingMethods() && !within(com.example.demo.auditoria.web.AuditoriaController)")
    public Object auditarLectura(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        
        // SOLO auditar exportaciones - nada más
        if (methodName.contains("export") || methodName.contains("exportar") || 
            methodName.equals("exportarContribuyentes") || methodName.equals("exportarRetenciones") ||
            methodName.equals("exportarComprobantes")) {
            return ejecutarConAuditoria(joinPoint, "EXPORT");
        }
        
        // NO auditar navegación, vistas, accesos a módulos - ejecutar sin auditoría
        return joinPoint.proceed();
    }
    
    /**
     * Intercepta errores en controladores
     */
    @AfterThrowing(pointcut = "controllerMethods()", throwing = "exception")
    public void auditarError(JoinPoint joinPoint, Throwable exception) {
        try {
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            String modulo = extraerModulo(className);
            String accion = extraerAccion(methodName);
            
            String descripcion = String.format("Error en %s.%s", className, methodName);
            
            auditoriaService.registrarError(accion, modulo, descripcion, exception.getMessage());
            
        } catch (Exception e) {
            log.error("Error al auditar excepción: {}", e.getMessage());
        }
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    /**
     * Ejecuta el método con auditoría
     */
    private Object ejecutarConAuditoria(ProceedingJoinPoint joinPoint, String accion) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String modulo = extraerModulo(className);
        
        try {
            // Ejecutar el método original
            Object result = joinPoint.proceed();
            
            // Determinar si fue exitoso
            boolean exitoso = esResultadoExitoso(result);
            
            if (exitoso) {
                // Crear descripción detallada
                String descripcion = crearDescripcion(accion, className, methodName, joinPoint.getArgs());
                
                // Registrar la acción
                auditoriaService.registrarAccion(accion, modulo, descripcion);
            }
            
            return result;
            
        } catch (Exception e) {
            // Registrar el error
            String descripcion = String.format("Error en %s.%s", className, methodName);
            auditoriaService.registrarError(accion, modulo, descripcion, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Extrae el módulo basado en el nombre de la clase
     */
    private String extraerModulo(String className) {
        if (className.toLowerCase().contains("contribuyente")) return "CONTRIBUYENTES";
        if (className.toLowerCase().contains("retencion")) return "RETENCIONES";
        if (className.toLowerCase().contains("comprobante")) return "COMPROBANTES";
        if (className.toLowerCase().contains("impuesto")) return "IMPUESTOS";
        if (className.toLowerCase().contains("declaracion")) return "DECLARACIONES";
        if (className.toLowerCase().contains("personal")) return "PERSONAL";
        if (className.toLowerCase().contains("rol")) return "ROLES";
        if (className.toLowerCase().contains("usuario")) return "USUARIOS";
        if (className.toLowerCase().contains("auditoria")) return "AUDITORIA";
        return "SISTEMA";
    }
    
    /**
     * Extrae la acción basada en el nombre del método
     */
    private String extraerAccion(String methodName) {
        if (methodName.contains("crear") || methodName.contains("save") || methodName.contains("add")) return "CREATE";
        if (methodName.contains("actualizar") || methodName.contains("update") || methodName.contains("edit")) return "UPDATE";
        if (methodName.contains("eliminar") || methodName.contains("delete") || methodName.contains("remove")) return "DELETE";
        if (methodName.contains("export")) return "EXPORT";
        if (methodName.contains("mostrar") || methodName.contains("index")) return "VIEW";
        return "ACTION";
    }
    
    /**
     * Extrae el nombre de la entidad basado en el nombre de la clase
     */
    private String extraerEntidad(String className) {
        if (className.toLowerCase().contains("contribuyente")) return "contribuyente";
        if (className.toLowerCase().contains("retencion")) return "retención";
        if (className.toLowerCase().contains("comprobante")) return "comprobante";
        if (className.toLowerCase().contains("impuesto")) return "impuesto";
        if (className.toLowerCase().contains("declaracion")) return "declaración";
        if (className.toLowerCase().contains("personal")) return "empleado";
        if (className.toLowerCase().contains("rol")) return "rol";
        if (className.toLowerCase().contains("usuario")) return "usuario";
        return "registro";
    }
    
    /**
     * Determina si el resultado fue exitoso
     */
    private boolean esResultadoExitoso(Object result) {
        if (result instanceof ResponseEntity) {
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            return response.getStatusCode().is2xxSuccessful();
        }
        return result != null;
    }
    
    /**
     * Crea una descripción detallada de la acción
     */
    private String crearDescripcion(String accion, String className, String methodName, Object[] args) {
        StringBuilder descripcion = new StringBuilder();
        
        String entidad = extraerEntidad(className);
        
        switch (accion) {
            case "CREATE":
                descripcion.append("Creó nuevo ").append(entidad.toLowerCase());
                break;
            case "UPDATE":
                descripcion.append("Actualizó ").append(entidad.toLowerCase());
                break;
            case "DELETE":
                descripcion.append("Eliminó ").append(entidad.toLowerCase());
                break;
            case "EXPORT":
                descripcion.append("Exportó datos de ").append(entidad.toLowerCase());
                break;
            default:
                descripcion.append("Realizó acción en ").append(entidad.toLowerCase());
        }
        
        // Agregar ID si está disponible en los parámetros
        if (args != null && args.length > 0) {
            for (Object arg : args) {
                if (arg instanceof Long || (arg instanceof String && arg.toString().matches("\\d+"))) {
                    descripcion.append(" (ID: ").append(arg.toString()).append(")");
                    break;
                }
            }
        }
        
        return descripcion.toString();
    }
    
    /**
     * Determina si un parámetro es relevante para la auditoría
     */
    private boolean isRelevantParameter(Object param) {
        if (param == null) return false;
        
        String paramStr = param.toString();
        
        // Filtrar parámetros no relevantes
        if (paramStr.contains("org.springframework") || 
            paramStr.contains("jakarta.servlet") ||
            paramStr.contains("HttpServlet") ||
            paramStr.length() > 100) {
            return false;
        }
        
        // Incluir IDs, nombres, etc.
        return param instanceof Number || 
               param instanceof String ||
               param.getClass().getSimpleName().endsWith("DTO");
    }
}
