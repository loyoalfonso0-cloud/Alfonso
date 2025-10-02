package com.example.demo.tributario.web;

import com.example.demo.tributario.service.TributarioService;
import com.example.demo.tributario.model.Declaracion;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class TributarioController {

    private final TributarioService tributarioService;

    @GetMapping("/tributario")
    public String index(Model model){
        model.addAttribute("contribuyentes", tributarioService.listarContribuyentes());
        model.addAttribute("impuestos", tributarioService.listarImpuestos());
        model.addAttribute("declaraciones", tributarioService.listarDeclaraciones());
        return "tributario/index";
    }





    @PostMapping("/tributario/declaraciones")
    @ResponseBody
    public ResponseEntity<Declaracion> crearDeclaracion(@RequestParam Long contribuyenteId,
                                                       @RequestParam Long impuestoId,
                                                       @RequestParam String periodo,
                                                       @RequestParam BigDecimal baseImponible,
                                                       @RequestParam BigDecimal monto){
        try {
            Declaracion declaracion = tributarioService.crearDeclaracion(contribuyenteId, impuestoId, periodo, baseImponible, monto);
            return ResponseEntity.ok(declaracion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/tributario/declaraciones/{id}")
    @ResponseBody
    public ResponseEntity<Declaracion> obtenerDeclaracion(@PathVariable Long id) {
        try {
            Declaracion declaracion = tributarioService.obtenerDeclaracionPorId(id);
            return ResponseEntity.ok(declaracion);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/tributario/declaraciones/{id}")
    @ResponseBody
    public ResponseEntity<Declaracion> actualizarDeclaracion(@PathVariable Long id,
                                                            @RequestParam Long contribuyenteId,
                                                            @RequestParam Long impuestoId,
                                                            @RequestParam String periodo,
                                                            @RequestParam BigDecimal baseImponible,
                                                            @RequestParam BigDecimal monto) {
        try {
            Declaracion declaracion = tributarioService.actualizarDeclaracion(id, contribuyenteId, impuestoId, periodo, baseImponible, monto);
            return ResponseEntity.ok(declaracion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/tributario/declaraciones/{id}")
    @ResponseBody
    public ResponseEntity<String> eliminarDeclaracion(@PathVariable Long id) {
        try {
            tributarioService.eliminarDeclaracion(id);
            return ResponseEntity.ok("Declaración eliminada exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("No se puede eliminar la declaración. Puede estar siendo utilizada en otras operaciones.");
        }
    }

    @PostMapping("/tributario/declaraciones/{id}/cambiar-estado")
    @ResponseBody
    public ResponseEntity<Declaracion> cambiarEstadoDeclaracion(@PathVariable Long id, @RequestParam String estado) {
        try {
            System.out.println("=== DEBUG CAMBIAR ESTADO DECLARACION ===");
            System.out.println("ID: " + id + ", Nuevo Estado: " + estado);
            Declaracion declaracion = tributarioService.cambiarEstadoDeclaracion(id, estado);
            System.out.println("Estado cambiado exitosamente");
            System.out.println("========================================");
            return ResponseEntity.ok(declaracion);
        } catch (IllegalArgumentException e) {
            System.out.println("Error al cambiar estado: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            System.out.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/tributario/declaraciones/export")
    public void exportarDeclaraciones(HttpServletResponse response) throws IOException {
        try {
            System.out.println("=== DEBUG EXPORTAR DECLARACIONES ===");
            tributarioService.exportarDeclaracionesExcel(response);
            System.out.println("Exportación completada exitosamente");
            System.out.println("===================================");
        } catch (Exception e) {
            System.out.println("Error al exportar declaraciones: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al generar el archivo Excel");
        }
    }

    @PostMapping("/tributario/pagos")
    @ResponseBody
    public ResponseEntity<Object> registrarPago(@RequestParam Long declaracionId,
                                               @RequestParam BigDecimal monto,
                                               @RequestParam(required = false) String fechaPago,
                                               @RequestParam String metodoPago,
                                               @RequestParam(required = false) String referencia) {
        try {
            System.out.println("=== DEBUG REGISTRAR PAGO ===");
            System.out.println("DeclaracionId: " + declaracionId);
            System.out.println("Monto: " + monto);
            System.out.println("FechaPago: " + fechaPago);
            System.out.println("Método: " + metodoPago);
            System.out.println("Referencia: " + referencia);
            
            // Validar parámetros requeridos
            if (declaracionId == null) {
                System.out.println("ERROR: ID de declaración es nulo");
                throw new IllegalArgumentException("ID de declaración es requerido");
            }
            if (monto == null) {
                System.out.println("ERROR: Monto es nulo");
                throw new IllegalArgumentException("Monto es requerido");
            }
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("ERROR: Monto debe ser mayor a cero: " + monto);
                throw new IllegalArgumentException("El monto debe ser mayor a cero");
            }
            if (metodoPago == null || metodoPago.trim().isEmpty()) {
                System.out.println("ERROR: Método de pago es nulo o vacío: '" + metodoPago + "'");
                throw new IllegalArgumentException("Debe seleccionar un método de pago válido");
            }
            
            Object pago = tributarioService.registrarPago(declaracionId, monto, metodoPago, referencia);
            System.out.println("Pago registrado exitosamente");
            System.out.println("===========================");
            return ResponseEntity.ok(pago);
        } catch (IllegalArgumentException e) {
            System.out.println("Error al registrar pago: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error inesperado al registrar pago: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al registrar el pago");
        }
    }

    // ==================== API ENDPOINTS PARA MODAL DE PAGOS ====================
    
    @GetMapping("/api/contribuyentes")
    @ResponseBody
    public ResponseEntity<?> listarContribuyentesApi() {
        try {
            return ResponseEntity.ok(tributarioService.listarContribuyentes());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al cargar contribuyentes: " + e.getMessage());
        }
    }
    
    @GetMapping("/api/declaraciones")
    @ResponseBody
    public ResponseEntity<?> listarDeclaracionesApi() {
        try {
            return ResponseEntity.ok(tributarioService.listarDeclaraciones());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al cargar declaraciones: " + e.getMessage());
        }
    }
}


