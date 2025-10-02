package com.example.demo.multas.web;

import com.example.demo.multas.dto.MultaRequest;
import com.example.demo.multas.dto.MultaResponse;
import com.example.demo.multas.model.EstadoMulta;
import com.example.demo.multas.model.TipoInfraccion;
import com.example.demo.multas.service.MultaService;
import com.example.demo.tributario.service.TributarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/multas")
@RequiredArgsConstructor
@Slf4j
public class MultaController {
    
    private final MultaService multaService;
    private final TributarioService tributarioService;
    
    /**
     * Página principal de multas
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('MULTAS_READ', 'MULTAS_GESTIONAR')")
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaRegistro") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {
        
        try {
            // Obtener multas paginadas
            Page<MultaResponse> multas = multaService.listarMultas(page, size, sortBy, sortDir);
            
            // Obtener estadísticas
            MultaService.MultaEstadisticas estadisticas = multaService.obtenerEstadisticas();
            
            // Obtener contribuyentes para el modal
            var contribuyentes = tributarioService.listarContribuyentes();
            
            // Agregar al modelo
            model.addAttribute("multas", multas);
            model.addAttribute("estadisticas", estadisticas);
            model.addAttribute("contribuyentes", contribuyentes);
            model.addAttribute("tiposInfraccion", TipoInfraccion.values());
            model.addAttribute("estadosMulta", EstadoMulta.values());
            
            // Parámetros de paginación
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", multas.getTotalPages());
            model.addAttribute("totalElements", multas.getTotalElements());
            model.addAttribute("size", size);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            
            return "multas/index";
            
        } catch (Exception e) {
            log.error("Error al cargar página de multas", e);
            model.addAttribute("error", "Error al cargar las multas: " + e.getMessage());
            return "error";
        }
    }
    
    /**
     * Ver detalle de multa
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('MULTAS_READ', 'MULTAS_GESTIONAR')")
    public String verMulta(@PathVariable Long id, Model model) {
        try {
            MultaResponse multa = multaService.obtenerMultaPorId(id);
            model.addAttribute("multa", multa);
            return "multas/detalle";
        } catch (Exception e) {
            log.error("Error al cargar multa: {}", id, e);
            model.addAttribute("error", "Error al cargar la multa: " + e.getMessage());
            return "error";
        }
    }
    
    // ==================== API ENDPOINTS ====================
    
    /**
     * API: Crear nueva multa
     */
    @PostMapping("/api")
    @PreAuthorize("hasAnyAuthority('MULTAS_WRITE', 'MULTAS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<MultaResponse> crearMultaApi(
            @Valid @RequestBody MultaRequest multaRequest,
            Authentication authentication) {
        try {
            multaRequest.setUsuarioRegistro(authentication.getName());
            MultaResponse multaCreada = multaService.crearMulta(multaRequest);
            return ResponseEntity.ok(multaCreada);
        } catch (Exception e) {
            log.error("Error al crear multa via API", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * API: Obtener multa por ID
     */
    @GetMapping("/api/{id}")
    @PreAuthorize("hasAnyAuthority('MULTAS_READ', 'MULTAS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<MultaResponse> obtenerMultaApi(@PathVariable Long id) {
        try {
            MultaResponse multa = multaService.obtenerMultaPorId(id);
            return ResponseEntity.ok(multa);
        } catch (Exception e) {
            log.error("Error al obtener multa via API: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * API: Listar multas
     */
    @GetMapping("/api")
    @PreAuthorize("hasAnyAuthority('MULTAS_READ', 'MULTAS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<Page<MultaResponse>> listarMultasApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaRegistro") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Page<MultaResponse> multas = multaService.listarMultas(page, size, sortBy, sortDir);
            return ResponseEntity.ok(multas);
        } catch (Exception e) {
            log.error("Error al listar multas via API", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * API: Actualizar multa
     */
    @PutMapping("/api/{id}")
    @PreAuthorize("hasAnyAuthority('MULTAS_WRITE', 'MULTAS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<MultaResponse> actualizarMultaApi(
            @PathVariable Long id,
            @Valid @RequestBody MultaRequest multaRequest,
            Authentication authentication) {
        try {
            multaRequest.setUsuarioRegistro(authentication.getName());
            MultaResponse multaActualizada = multaService.actualizarMulta(id, multaRequest);
            return ResponseEntity.ok(multaActualizada);
        } catch (Exception e) {
            log.error("Error al actualizar multa via API: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * API: Anular multa
     */
    @PutMapping("/api/{id}/anular")
    @PreAuthorize("hasAnyAuthority('MULTAS_WRITE', 'MULTAS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<MultaResponse> anularMultaApi(
            @PathVariable Long id,
            @RequestParam String motivo,
            Authentication authentication) {
        try {
            MultaResponse multaAnulada = multaService.anularMulta(id, motivo, authentication.getName());
            return ResponseEntity.ok(multaAnulada);
        } catch (Exception e) {
            log.error("Error al anular multa via API: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * API: Registrar pago de multa
     */
    @PutMapping("/api/{id}/pagar")
    @PreAuthorize("hasAnyAuthority('MULTAS_WRITE', 'MULTAS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<MultaResponse> pagarMultaApi(
            @PathVariable Long id,
            @RequestParam BigDecimal monto,
            Authentication authentication) {
        try {
            MultaResponse multaPagada = multaService.registrarPago(id, monto, authentication.getName());
            return ResponseEntity.ok(multaPagada);
        } catch (Exception e) {
            log.error("Error al registrar pago de multa via API: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * API: Buscar multas por contribuyente
     */
    @GetMapping("/api/contribuyente/{contribuyenteId}")
    @PreAuthorize("hasAnyAuthority('MULTAS_READ', 'MULTAS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<Page<MultaResponse>> multasPorContribuyenteApi(
            @PathVariable Long contribuyenteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<MultaResponse> multas = multaService.buscarPorContribuyente(contribuyenteId, page, size);
            return ResponseEntity.ok(multas);
        } catch (Exception e) {
            log.error("Error al buscar multas por contribuyente via API: {}", contribuyenteId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * API: Buscar multas por estado
     */
    @GetMapping("/api/estado/{estado}")
    @PreAuthorize("hasAnyAuthority('MULTAS_READ', 'MULTAS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<Page<MultaResponse>> multasPorEstadoApi(
            @PathVariable EstadoMulta estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<MultaResponse> multas = multaService.buscarPorEstado(estado, page, size);
            return ResponseEntity.ok(multas);
        } catch (Exception e) {
            log.error("Error al buscar multas por estado via API: {}", estado, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * API: Obtener estadísticas
     */
    @GetMapping("/api/estadisticas")
    @PreAuthorize("hasAnyAuthority('MULTAS_READ', 'MULTAS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<MultaService.MultaEstadisticas> obtenerEstadisticasApi() {
        try {
            MultaService.MultaEstadisticas estadisticas = multaService.obtenerEstadisticas();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            log.error("Error al obtener estadísticas via API", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
