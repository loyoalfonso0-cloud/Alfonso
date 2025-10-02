package com.example.demo.transacciones.web;

import com.example.demo.transacciones.dto.TransaccionRequest;
import com.example.demo.transacciones.dto.TransaccionResponse;
import com.example.demo.transacciones.model.EstadoTransaccion;
import com.example.demo.transacciones.model.TipoTransaccion;
import com.example.demo.transacciones.service.TransaccionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/transacciones")
public class TransaccionController {

    @Autowired
    private TransaccionService transaccionService;

    // ==================== ENDPOINTS WEB ====================

    @GetMapping
    @PreAuthorize("hasAnyAuthority('TRANSACCIONES_READ', 'TRANSACCIONES_WRITE', 'TRANSACCIONES_GESTIONAR')")
    public String index(Model model) {
        try {
            // Obtener estadísticas para el dashboard
            Map<String, Object> estadisticas = transaccionService.obtenerEstadisticas();
            model.addAttribute("estadisticas", estadisticas);
            
            // Obtener transacciones recientes
            List<TransaccionResponse> transaccionesRecientes = transaccionService.obtenerTransaccionesRecientes(10);
            model.addAttribute("transaccionesRecientes", transaccionesRecientes);
            
            // Obtener todas las transacciones para la tabla principal
            Page<TransaccionResponse> transacciones = transaccionService.listarTransacciones(0, 20, "fechaTransaccion", "desc");
            model.addAttribute("transacciones", transacciones);
            
            // Agregar enums para los filtros
            model.addAttribute("tiposTransaccion", TipoTransaccion.values());
            model.addAttribute("estadosTransaccion", EstadoTransaccion.values());
            
            return "transacciones/index";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar las transacciones: " + e.getMessage());
            return "transacciones/index";
        }
    }

    @GetMapping("/nueva")
    @PreAuthorize("hasAnyAuthority('TRANSACCIONES_WRITE', 'TRANSACCIONES_GESTIONAR')")
    public String nuevaTransaccion(Model model) {
        model.addAttribute("tiposTransaccion", TipoTransaccion.values());
        return "transacciones/nueva";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TRANSACCIONES_READ', 'TRANSACCIONES_WRITE', 'TRANSACCIONES_GESTIONAR')")
    public String verTransaccion(@PathVariable Long id, Model model) {
        return transaccionService.obtenerTransaccionPorId(id)
                .map(transaccion -> {
                    model.addAttribute("transaccion", transaccion);
                    return "transacciones/detalle";
                })
                .orElse("redirect:/transacciones?error=Transacción no encontrada");
    }

    // ==================== API REST ====================

    @GetMapping("/api")
    @PreAuthorize("hasAnyAuthority('TRANSACCIONES_READ', 'TRANSACCIONES_WRITE', 'TRANSACCIONES_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<Page<TransaccionResponse>> listarTransaccionesApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaTransaccion") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Page<TransaccionResponse> transacciones = transaccionService.listarTransacciones(page, size, sortBy, sortDir);
            return ResponseEntity.ok(transacciones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/{id}")
    @PreAuthorize("hasAnyAuthority('TRANSACCIONES_READ', 'TRANSACCIONES_WRITE', 'TRANSACCIONES_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<TransaccionResponse> obtenerTransaccionApi(@PathVariable Long id) {
        return transaccionService.obtenerTransaccionPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/api")
    @PreAuthorize("hasAnyAuthority('TRANSACCIONES_WRITE', 'TRANSACCIONES_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<?> crearTransaccionApi(@Valid @RequestBody TransaccionRequest request) {
        try {
            TransaccionResponse transaccion = transaccionService.crearTransaccion(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(transaccion);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PutMapping("/api/{id}")
    @PreAuthorize("hasAnyAuthority('TRANSACCIONES_WRITE', 'TRANSACCIONES_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<?> actualizarTransaccionApi(@PathVariable Long id, @Valid @RequestBody TransaccionRequest request) {
        try {
            TransaccionResponse transaccion = transaccionService.actualizarTransaccion(id, request);
            return ResponseEntity.ok(transaccion);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/api/{id}")
    @PreAuthorize("hasAnyAuthority('TRANSACCIONES_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<?> eliminarTransaccionApi(@PathVariable Long id) {
        try {
            transaccionService.eliminarTransaccion(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/api/{id}/procesar")
    @PreAuthorize("hasAnyAuthority('TRANSACCIONES_WRITE', 'TRANSACCIONES_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<?> procesarTransaccionApi(@PathVariable Long id) {
        try {
            TransaccionResponse transaccion = transaccionService.procesarTransaccion(id);
            return ResponseEntity.ok(transaccion);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/api/{id}/confirmar")
    @PreAuthorize("hasAnyAuthority('TRANSACCIONES_WRITE', 'TRANSACCIONES_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<?> confirmarTransaccionApi(@PathVariable Long id) {
        try {
            TransaccionResponse transaccion = transaccionService.confirmarTransaccion(id);
            return ResponseEntity.ok(transaccion);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/api/{id}/anular")
    @PreAuthorize("hasAnyAuthority('TRANSACCIONES_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<?> anularTransaccionApi(@PathVariable Long id, @RequestParam String motivo) {
        try {
            TransaccionResponse transaccion = transaccionService.anularTransaccion(id, motivo);
            return ResponseEntity.ok(transaccion);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/api/buscar")
    @PreAuthorize("hasAnyAuthority('TRANSACCIONES_READ', 'TRANSACCIONES_WRITE', 'TRANSACCIONES_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<Page<TransaccionResponse>> busquedaAvanzadaApi(
            @RequestParam(required = false) String numeroTransaccion,
            @RequestParam(required = false) Long contribuyenteId,
            @RequestParam(required = false) EstadoTransaccion estado,
            @RequestParam(required = false) TipoTransaccion tipoTransaccion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(required = false) BigDecimal montoMinimo,
            @RequestParam(required = false) BigDecimal montoMaximo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Page<TransaccionResponse> resultados = transaccionService.busquedaAvanzada(
                    numeroTransaccion, contribuyenteId, estado, tipoTransaccion,
                    fechaInicio, fechaFin, montoMinimo, montoMaximo, page, size
            );
            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/estadisticas")
    @PreAuthorize("hasAnyAuthority('TRANSACCIONES_READ', 'TRANSACCIONES_WRITE', 'TRANSACCIONES_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasApi() {
        try {
            Map<String, Object> estadisticas = transaccionService.obtenerEstadisticas();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/recientes")
    @PreAuthorize("hasAnyAuthority('TRANSACCIONES_READ', 'TRANSACCIONES_WRITE', 'TRANSACCIONES_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<List<TransaccionResponse>> obtenerTransaccionesRecientesApi(
            @RequestParam(defaultValue = "10") int limite) {
        try {
            List<TransaccionResponse> transacciones = transaccionService.obtenerTransaccionesRecientes(limite);
            return ResponseEntity.ok(transacciones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/contribuyente/{contribuyenteId}")
    @PreAuthorize("hasAnyAuthority('TRANSACCIONES_READ', 'TRANSACCIONES_WRITE', 'TRANSACCIONES_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<Page<TransaccionResponse>> obtenerTransaccionesPorContribuyenteApi(
            @PathVariable Long contribuyenteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<TransaccionResponse> transacciones = transaccionService.obtenerTransaccionesPorContribuyente(contribuyenteId, page, size);
            return ResponseEntity.ok(transacciones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/reporte")
    @PreAuthorize("hasAnyAuthority('TRANSACCIONES_READ', 'TRANSACCIONES_WRITE', 'TRANSACCIONES_GESTIONAR')")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> generarReporteApi(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            List<Map<String, Object>> reporte = transaccionService.generarReportePorPeriodo(fechaInicio, fechaFin);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
