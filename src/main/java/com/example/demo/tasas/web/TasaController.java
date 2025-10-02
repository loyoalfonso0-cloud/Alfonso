package com.example.demo.tasas.web;

import com.example.demo.tasas.dto.TasaRequest;
import com.example.demo.tasas.dto.TasaResponse;
import com.example.demo.tasas.model.EstadoTasa;
import com.example.demo.tasas.model.TipoTasa;
import com.example.demo.tasas.service.TasaService;
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
 * Controlador completo para Tasas Municipales
 */
@Controller
@RequestMapping("/tasas")
@RequiredArgsConstructor
@Slf4j
public class TasaController {

    private final TasaService tasaService;
    private final ContribuyenteService contribuyenteService;

    /**
     * Página principal de tasas
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('TASAS_READ', 'TASAS_WRITE', 'TASAS_GESTIONAR')")
    public String index(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size) {
        
        try {
            log.info("=== ACCEDIENDO A TASAS PRINCIPALES ===");
            
            // Obtener tasas paginadas
            Page<TasaResponse> tasas = tasaService.listarTasas(page, size);
            
            // Obtener estadísticas
            Map<String, Object> estadisticas = tasaService.obtenerEstadisticas();
            
            // Obtener contribuyentes activos para el modal
            var contribuyentes = contribuyenteService.listarActivos();
            
            // Agregar datos al modelo
            model.addAttribute("tasas", tasas);
            model.addAttribute("estadisticas", estadisticas);
            model.addAttribute("contribuyentes", contribuyentes);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", tasas.getTotalPages());
            model.addAttribute("tiposTasa", TipoTasa.values());
            model.addAttribute("estadosTasa", EstadoTasa.values());
            
            // Estadísticas para las tarjetas
            model.addAttribute("totalTasas", estadisticas.getOrDefault("totalTasas", 0L));
            
            @SuppressWarnings("unchecked")
            Map<String, Long> porEstado = (Map<String, Long>) estadisticas.getOrDefault("porEstado", new HashMap<String, Long>());
            model.addAttribute("activasTasas", porEstado.getOrDefault("ACTIVA", 0L));
            model.addAttribute("pagadasTasas", porEstado.getOrDefault("PAGADA", 0L));
            model.addAttribute("vencidasTasas", porEstado.getOrDefault("VENCIDA", 0L));
            
            log.info("=== DATOS CARGADOS CORRECTAMENTE ===");
            return "tasas/index";
            
        } catch (Exception e) {
            log.error("=== ERROR AL CARGAR TASAS ===", e);
            // En caso de error, mostrar datos básicos
            model.addAttribute("totalTasas", 0);
            model.addAttribute("activasTasas", 0);
            model.addAttribute("pagadasTasas", 0);
            model.addAttribute("vencidasTasas", 0);
            model.addAttribute("error", "Error al cargar datos: " + e.getMessage());
            return "tasas/index";
        }
    }

    /**
     * API - Crear nueva tasa
     */
    @PostMapping("/api")
    @PreAuthorize("hasAnyAuthority('TASAS_WRITE', 'TASAS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<?> crearTasa(@RequestBody TasaRequest request, Principal principal) {
        try {
            request.setUsuarioRegistro(principal.getName());
            TasaResponse tasaCreada = tasaService.crearTasa(request);
            return ResponseEntity.ok(tasaCreada);
        } catch (Exception e) {
            log.error("Error al crear tasa", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al crear la tasa: " + e.getMessage()));
        }
    }

    /**
     * API - Obtener tasa por ID
     */
    @GetMapping("/api/{id}")
    @PreAuthorize("hasAnyAuthority('TASAS_READ', 'TASAS_WRITE', 'TASAS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<TasaResponse> obtenerTasa(@PathVariable Long id) {
        try {
            return tasaService.obtenerTasaPorId(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error al obtener tasa", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API - Actualizar tasa
     */
    @PutMapping("/api/{id}")
    @PreAuthorize("hasAnyAuthority('TASAS_WRITE', 'TASAS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<?> actualizarTasa(@PathVariable Long id, @RequestBody TasaRequest request, Principal principal) {
        try {
            request.setUsuarioRegistro(principal.getName());
            TasaResponse tasaActualizada = tasaService.actualizarTasa(id, request);
            return ResponseEntity.ok(tasaActualizada);
        } catch (Exception e) {
            log.error("Error al actualizar tasa", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al actualizar la tasa: " + e.getMessage()));
        }
    }

    /**
     * API - Eliminar tasa
     */
    @DeleteMapping("/api/{id}")
    @PreAuthorize("hasAnyAuthority('TASAS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<?> eliminarTasa(@PathVariable Long id) {
        try {
            tasaService.eliminarTasa(id);
            return ResponseEntity.ok(Map.of("mensaje", "Tasa eliminada correctamente"));
        } catch (Exception e) {
            log.error("Error al eliminar tasa", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al eliminar la tasa: " + e.getMessage()));
        }
    }

    /**
     * API - Registrar pago de tasa
     */
    @PostMapping("/api/{id}/pago")
    @PreAuthorize("hasAnyAuthority('TASAS_WRITE', 'TASAS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<?> registrarPagoTasa(@PathVariable Long id, @RequestBody Map<String, Object> pagoData, Principal principal) {
        try {
            // Obtener la tasa
            var tasaOpt = tasaService.obtenerTasaPorId(id);
            if (tasaOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            var tasa = tasaOpt.get();
            
            // Validar que la tasa permita pagos
            if (tasa.getEstado().name().equals("PAGADA")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "La tasa ya está completamente pagada"));
            }

            // Procesar el pago interno de la tasa
            BigDecimal montoPago = new BigDecimal(pagoData.get("monto").toString());
            
            if (montoPago.compareTo(BigDecimal.ZERO) <= 0 || montoPago.compareTo(tasa.getSaldoPendiente()) > 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Monto de pago inválido"));
            }

            // Actualizar el saldo de la tasa
            TasaResponse tasaActualizada = tasaService.registrarPago(id, montoPago, principal.getName());
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Pago registrado correctamente",
                "montoPagado", montoPago,
                "tasa", tasaActualizada
            ));
        } catch (Exception e) {
            log.error("Error al registrar pago de tasa", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al registrar el pago: " + e.getMessage()));
        }
    }
}
