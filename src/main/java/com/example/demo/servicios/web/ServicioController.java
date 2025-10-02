package com.example.demo.servicios.web;

import com.example.demo.servicios.domain.EstadoServicio;
import com.example.demo.servicios.domain.TipoServicio;
import com.example.demo.servicios.dto.ServicioRequest;
import com.example.demo.servicios.dto.ServicioResponse;
import com.example.demo.servicios.service.ServicioService;
import com.example.demo.tributario.service.ContribuyenteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador completo para Servicios Municipales
 */
@Controller
@RequestMapping("/servicios")
@RequiredArgsConstructor
@Slf4j
public class ServicioController {

    private final ServicioService servicioService;
    private final ContribuyenteService contribuyenteService;

    /**
     * Página principal de servicios
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('SERVICIOS_READ', 'SERVICIOS_WRITE', 'SERVICIOS_GESTIONAR')")
    public String index(Model model, 
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("=== CARGANDO MÓDULO DE SERVICIOS ===");
            
            // Cargar servicios paginados
            Page<ServicioResponse> serviciosPage = servicioService.obtenerServiciosPaginados(page, size, null, null, null, null);
            model.addAttribute("servicios", serviciosPage);
            
            // Cargar estadísticas
            @SuppressWarnings("unchecked")
            Map<String, Object> estadisticas = servicioService.obtenerEstadisticas();
            model.addAttribute("estadisticas", estadisticas);
            
            // Cargar contribuyentes activos para el formulario
            model.addAttribute("contribuyentes", contribuyenteService.listarActivos());
            
            // Cargar enums
            model.addAttribute("tiposServicio", TipoServicio.values());
            model.addAttribute("estadosServicio", EstadoServicio.values());
            
            log.info("=== DATOS CARGADOS CORRECTAMENTE ===");
            return "servicios/index";
            
        } catch (Exception e) {
            log.error("Error al cargar módulo de servicios", e);
            model.addAttribute("error", "Error al cargar los datos: " + e.getMessage());
            return "error";
        }
    }

    /**
     * API - Crear servicio
     */
    @PostMapping("/api")
    @PreAuthorize("hasAnyAuthority('SERVICIOS_WRITE', 'SERVICIOS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<?> crearServicio(@RequestBody ServicioRequest request, Principal principal) {
        try {
            request.setUsuarioRegistro(principal.getName());
            ServicioResponse servicio = servicioService.crearServicio(request);
            return ResponseEntity.ok(servicio);
        } catch (Exception e) {
            log.error("Error al crear servicio", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al crear el servicio: " + e.getMessage()));
        }
    }

    /**
     * API - Obtener servicio por ID
     */
    @GetMapping("/api/{id}")
    @PreAuthorize("hasAnyAuthority('SERVICIOS_READ', 'SERVICIOS_WRITE', 'SERVICIOS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<ServicioResponse> obtenerServicio(@PathVariable Long id) {
        return servicioService.obtenerServicioPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * API - Actualizar servicio
     */
    @PutMapping("/api/{id}")
    @PreAuthorize("hasAnyAuthority('SERVICIOS_WRITE', 'SERVICIOS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<?> actualizarServicio(@PathVariable Long id, @RequestBody ServicioRequest request, Principal principal) {
        try {
            request.setUsuarioRegistro(principal.getName());
            ServicioResponse servicio = servicioService.actualizarServicio(id, request);
            return ResponseEntity.ok(servicio);
        } catch (Exception e) {
            log.error("Error al actualizar servicio", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al actualizar el servicio: " + e.getMessage()));
        }
    }

    /**
     * API - Eliminar servicio
     */
    @DeleteMapping("/api/{id}")
    @PreAuthorize("hasAnyAuthority('SERVICIOS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<?> eliminarServicio(@PathVariable Long id) {
        try {
            servicioService.eliminarServicio(id);
            return ResponseEntity.ok(Map.of("mensaje", "Servicio eliminado correctamente"));
        } catch (Exception e) {
            log.error("Error al eliminar servicio", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al eliminar el servicio: " + e.getMessage()));
        }
    }

    /**
     * API - Registrar pago de servicio
     */
    @PostMapping("/api/{id}/pago")
    @PreAuthorize("hasAnyAuthority('SERVICIOS_WRITE', 'SERVICIOS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<?> registrarPagoServicio(@PathVariable Long id, @RequestBody Map<String, Object> pagoData, Principal principal) {
        try {
            // Obtener el servicio
            var servicioOpt = servicioService.obtenerServicioPorId(id);
            if (servicioOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            var servicio = servicioOpt.get();
            
            // Validar que el servicio permita pagos
            if (!servicio.isPermiteFacturacion()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El servicio no permite pagos en su estado actual"));
            }

            // Procesar el pago interno del servicio
            BigDecimal montoPago = new BigDecimal(pagoData.get("monto").toString());
            
            if (montoPago.compareTo(BigDecimal.ZERO) <= 0 || montoPago.compareTo(servicio.getSaldoPendiente()) > 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Monto de pago inválido"));
            }

            // Actualizar el saldo del servicio
            ServicioResponse servicioActualizado = servicioService.registrarPago(id, montoPago, principal.getName());
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Pago registrado correctamente",
                "montoPagado", montoPago,
                "servicio", servicioActualizado
            ));
        } catch (Exception e) {
            log.error("Error al registrar pago de servicio", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al registrar el pago: " + e.getMessage()));
        }
    }

    /**
     * API - Cambiar estado de servicio
     */
    @PutMapping("/api/{id}/estado")
    @PreAuthorize("hasAnyAuthority('SERVICIOS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<?> cambiarEstadoServicio(@PathVariable Long id, @RequestBody Map<String, String> estadoData, Principal principal) {
        try {
            EstadoServicio nuevoEstado = EstadoServicio.valueOf(estadoData.get("estado"));
            ServicioResponse servicio = servicioService.cambiarEstado(id, nuevoEstado, principal.getName());
            return ResponseEntity.ok(Map.of(
                "mensaje", "Estado cambiado correctamente",
                "servicio", servicio
            ));
        } catch (Exception e) {
            log.error("Error al cambiar estado de servicio", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al cambiar el estado: " + e.getMessage()));
        }
    }

    /**
     * API - Buscar servicios con filtros
     */
    @GetMapping("/api/buscar")
    @PreAuthorize("hasAnyAuthority('SERVICIOS_READ', 'SERVICIOS_WRITE', 'SERVICIOS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<Page<ServicioResponse>> buscarServicios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String numeroServicio,
            @RequestParam(required = false) String contribuyenteNombre,
            @RequestParam(required = false) EstadoServicio estado,
            @RequestParam(required = false) TipoServicio tipoServicio) {
        
        Page<ServicioResponse> servicios = servicioService.obtenerServiciosPaginados(
                page, size, numeroServicio, contribuyenteNombre, estado, tipoServicio);
        return ResponseEntity.ok(servicios);
    }

    /**
     * API - Obtener estadísticas
     */
    @GetMapping("/api/estadisticas")
    @PreAuthorize("hasAnyAuthority('SERVICIOS_READ', 'SERVICIOS_WRITE', 'SERVICIOS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> estadisticas = servicioService.obtenerEstadisticas();
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * API - Obtener tipos de servicio
     */
    @GetMapping("/api/tipos")
    @PreAuthorize("hasAnyAuthority('SERVICIOS_READ', 'SERVICIOS_WRITE', 'SERVICIOS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerTiposServicio() {
        Map<String, Object> tipos = new HashMap<>();
        for (TipoServicio tipo : TipoServicio.values()) {
            Map<String, Object> tipoInfo = new HashMap<>();
            tipoInfo.put("descripcion", tipo.getDescripcion());
            tipoInfo.put("requiereMedidor", tipo.requiereMedidor());
            tipoInfo.put("esFacturablePorConsumo", tipo.esFacturablePorConsumo());
            tipoInfo.put("esTarifaFija", tipo.esTarifaFija());
            tipos.put(tipo.name(), tipoInfo);
        }
        return ResponseEntity.ok(tipos);
    }

    /**
     * API - Obtener estados de servicio
     */
    @GetMapping("/api/estados")
    @PreAuthorize("hasAnyAuthority('SERVICIOS_READ', 'SERVICIOS_WRITE', 'SERVICIOS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerEstadosServicio() {
        Map<String, Object> estados = new HashMap<>();
        for (EstadoServicio estado : EstadoServicio.values()) {
            Map<String, Object> estadoInfo = new HashMap<>();
            estadoInfo.put("descripcion", estado.getDescripcion());
            estadoInfo.put("colorClass", estado.getColorClass());
            estadoInfo.put("esOperativo", estado.esOperativo());
            estadoInfo.put("permiteFacturacion", estado.permiteFacturacion());
            estadoInfo.put("puedeSerCortado", estado.puedeSerCortado());
            estadoInfo.put("puedeSerReactivado", estado.puedeSerReactivado());
            estados.put(estado.name(), estadoInfo);
        }
        return ResponseEntity.ok(estados);
    }
}
