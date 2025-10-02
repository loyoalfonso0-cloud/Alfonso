package com.example.demo.pagos.web;

import com.example.demo.pagos.dto.PagoRequest;
import com.example.demo.pagos.dto.PagoResponse;
import com.example.demo.pagos.model.EstadoPago;
import com.example.demo.pagos.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/pagos")
@RequiredArgsConstructor
@Slf4j
public class PagoController {

    private final PagoService pagoService;

    /**
     * Página principal de pagos
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('PAGOS_READ', 'PAGOS_GESTIONAR')")
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        try {
            Page<PagoResponse> pagos = pagoService.listarPagos(page, size);
            List<PagoResponse> pagosRecientes = pagoService.obtenerPagosRecientes(5);
            List<PagoResponse> pagosDelDia = pagoService.obtenerPagosDelDia();

            model.addAttribute("pagos", pagos);
            model.addAttribute("pagosRecientes", pagosRecientes);
            model.addAttribute("pagosDelDia", pagosDelDia);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", pagos.getTotalPages());
            
            return "pagos/index";
        } catch (Exception e) {
            log.error("Error al cargar página de pagos", e);
            model.addAttribute("error", "Error al cargar los pagos: " + e.getMessage());
            return "pagos/index";
        }
    }

    /**
     * Formulario para nuevo pago
     */
    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyAuthority('PAGOS_WRITE', 'PAGOS_GESTIONAR')")
    public String nuevoPago(Model model) {
        model.addAttribute("pagoRequest", new PagoRequest());
        return "pagos/nuevo";
    }

    /**
     * Crear nuevo pago
     */
    @PostMapping("/crear")
    @PreAuthorize("hasAnyAuthority('PAGOS_WRITE', 'PAGOS_GESTIONAR')")
    public String crearPago(
            @Valid @ModelAttribute PagoRequest pagoRequest,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            pagoRequest.setUsuarioRegistro(authentication.getName());
            PagoResponse pago = pagoService.crearPago(pagoRequest);
            
            redirectAttributes.addFlashAttribute("success", 
                "Pago creado exitosamente con ID: " + pago.getId());
            return "redirect:/pagos";
            
        } catch (Exception e) {
            log.error("Error al crear pago", e);
            redirectAttributes.addFlashAttribute("error", 
                "Error al crear el pago: " + e.getMessage());
            return "redirect:/pagos/nuevo";
        }
    }

    /**
     * Ver detalle de pago
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PAGOS_READ', 'PAGOS_GESTIONAR')")
    public String verPago(@PathVariable Long id, Model model) {
        try {
            PagoResponse pago = pagoService.obtenerPagoPorId(id)
                    .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
            
            model.addAttribute("pago", pago);
            return "pagos/detalle";
            
        } catch (Exception e) {
            log.error("Error al cargar pago con ID: {}", id, e);
            model.addAttribute("error", "Error al cargar el pago: " + e.getMessage());
            return "redirect:/pagos";
        }
    }

    /**
     * Pagos por contribuyente
     */
    @GetMapping("/contribuyente/{contribuyenteId}")
    @PreAuthorize("hasAnyAuthority('PAGOS_READ', 'PAGOS_GESTIONAR')")
    public String pagosPorContribuyente(@PathVariable Long contribuyenteId, Model model) {
        try {
            List<PagoResponse> pagos = pagoService.obtenerPagosPorContribuyente(contribuyenteId);
            BigDecimal totalPagado = pagoService.obtenerTotalPagadoPorContribuyente(contribuyenteId);
            
            model.addAttribute("pagos", pagos);
            model.addAttribute("totalPagado", totalPagado);
            model.addAttribute("contribuyenteId", contribuyenteId);
            
            return "pagos/por-contribuyente";
            
        } catch (Exception e) {
            log.error("Error al cargar pagos del contribuyente: {}", contribuyenteId, e);
            model.addAttribute("error", "Error al cargar los pagos: " + e.getMessage());
            return "redirect:/pagos";
        }
    }

    // ==================== API REST ENDPOINTS ====================

    /**
     * API: Crear pago
     */
    @PostMapping("/api")
    @PreAuthorize("hasAnyAuthority('PAGOS_WRITE', 'PAGOS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<PagoResponse> crearPagoApi(
            @Valid @RequestBody PagoRequest pagoRequest,
            Authentication authentication) {
        
        try {
            pagoRequest.setUsuarioRegistro(authentication.getName());
            PagoResponse pago = pagoService.crearPago(pagoRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(pago);
            
        } catch (Exception e) {
            log.error("Error al crear pago via API", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API: Obtener pago por ID
     */
    @GetMapping("/api/{id}")
    @PreAuthorize("hasAnyAuthority('PAGOS_READ', 'PAGOS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<PagoResponse> obtenerPagoApi(@PathVariable Long id) {
        return pagoService.obtenerPagoPorId(id)
                .map(pago -> ResponseEntity.ok(pago))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * API: Listar pagos
     */
    @GetMapping("/api")
    @PreAuthorize("hasAnyAuthority('PAGOS_READ', 'PAGOS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<Page<PagoResponse>> listarPagosApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Page<PagoResponse> pagos = pagoService.listarPagos(page, size);
            return ResponseEntity.ok(pagos);
        } catch (Exception e) {
            log.error("Error al listar pagos via API", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API: Procesar pago
     */
    @PutMapping("/api/{id}/procesar")
    @PreAuthorize("hasAnyAuthority('PAGOS_WRITE', 'PAGOS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<PagoResponse> procesarPagoApi(
            @PathVariable Long id,
            Authentication authentication) {
        
        try {
            PagoResponse pago = pagoService.procesarPago(id, authentication.getName());
            return ResponseEntity.ok(pago);
        } catch (Exception e) {
            log.error("Error al procesar pago via API: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API: Confirmar pago
     */
    @PutMapping("/api/{id}/confirmar")
    @PreAuthorize("hasAnyAuthority('PAGOS_WRITE', 'PAGOS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<PagoResponse> confirmarPagoApi(
            @PathVariable Long id,
            Authentication authentication) {
        
        try {
            PagoResponse pago = pagoService.confirmarPago(id, authentication.getName());
            return ResponseEntity.ok(pago);
        } catch (Exception e) {
            log.error("Error al confirmar pago via API: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API: Anular pago
     */
    @PutMapping("/api/{id}/anular")
    @PreAuthorize("hasAnyAuthority('PAGOS_WRITE', 'PAGOS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<PagoResponse> anularPagoApi(
            @PathVariable Long id,
            Authentication authentication) {
        
        try {
            PagoResponse pago = pagoService.anularPago(id, authentication.getName());
            return ResponseEntity.ok(pago);
        } catch (Exception e) {
            log.error("Error al anular pago via API: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API: Pagos por contribuyente
     */
    @GetMapping("/api/contribuyente/{contribuyenteId}")
    @PreAuthorize("hasAnyAuthority('PAGOS_READ', 'PAGOS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<List<PagoResponse>> pagosPorContribuyenteApi(@PathVariable Long contribuyenteId) {
        try {
            List<PagoResponse> pagos = pagoService.obtenerPagosPorContribuyente(contribuyenteId);
            return ResponseEntity.ok(pagos);
        } catch (Exception e) {
            log.error("Error al obtener pagos por contribuyente via API: {}", contribuyenteId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API: Pagos por estado
     */
    @GetMapping("/api/estado/{estado}")
    @PreAuthorize("hasAnyAuthority('PAGOS_READ', 'PAGOS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<List<PagoResponse>> pagosPorEstadoApi(@PathVariable EstadoPago estado) {
        try {
            List<PagoResponse> pagos = pagoService.obtenerPagosPorEstado(estado);
            return ResponseEntity.ok(pagos);
        } catch (Exception e) {
            log.error("Error al obtener pagos por estado via API: {}", estado, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API: Buscar pagos por concepto
     */
    @GetMapping("/api/buscar")
    @PreAuthorize("hasAnyAuthority('PAGOS_READ', 'PAGOS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<List<PagoResponse>> buscarPagosApi(@RequestParam String concepto) {
        try {
            List<PagoResponse> pagos = pagoService.buscarPagosPorConcepto(concepto);
            return ResponseEntity.ok(pagos);
        } catch (Exception e) {
            log.error("Error al buscar pagos via API: {}", concepto, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API: Actualizar pago
     */
    @PutMapping("/api/{id}")
    @PreAuthorize("hasAnyAuthority('PAGOS_WRITE', 'PAGOS_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<PagoResponse> actualizarPagoApi(
            @PathVariable Long id,
            @Valid @RequestBody PagoRequest pagoRequest,
            Authentication authentication) {
        try {
            pagoRequest.setUsuarioRegistro(authentication.getName());
            PagoResponse pagoActualizado = pagoService.actualizarPago(id, pagoRequest);
            return ResponseEntity.ok(pagoActualizado);
        } catch (Exception e) {
            log.error("Error al actualizar pago via API: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Exportar pagos a Excel
     */
    @GetMapping("/exportar")
    @PreAuthorize("hasAnyAuthority('PAGOS_READ', 'PAGOS_GESTIONAR')")
    public void exportarPagos(HttpServletResponse response) throws IOException {
        try {
            pagoService.exportarPagosExcel(response);
        } catch (Exception e) {
            log.error("Error al exportar pagos", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al exportar pagos");
        }
    }
}
