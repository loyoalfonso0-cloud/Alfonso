package com.example.demo.tributario.web;

import com.example.demo.tributario.model.Retencion;
import com.example.demo.tributario.service.RetencionService;
import com.example.demo.tributario.service.ContribuyenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/tributario/retenciones")
@RequiredArgsConstructor
@Slf4j
public class RetencionController {
    
    private final RetencionService retencionService;
    private final ContribuyenteService contribuyenteService;
    
    @GetMapping
    @PreAuthorize("hasAnyAuthority('RETENCIONES_READ','RETENCIONES_VER','RETENCIONES_GESTIONAR') or hasRole('ADMIN_TRIBUTARIO')")
    public String listar(Model model) {
        try {
            model.addAttribute("retenciones", retencionService.obtenerTodas());
            model.addAttribute("estadisticas", obtenerEstadisticasPrivadas());
            model.addAttribute("contribuyentes", contribuyenteService.listarActivos());
            return "tributario/retenciones";
        } catch (Exception e) {
            log.error("Error al cargar retenciones", e);
            model.addAttribute("error", "Error al cargar las retenciones: " + e.getMessage());
            return "tributario/retenciones";
        }
    }
    
    @PostMapping
    @ResponseBody
    public ResponseEntity<?> crear(@Valid @RequestBody Retencion retencion) {
        try {
            Retencion saved = retencionService.guardar(retencion);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("Error al crear retención", e);
            return ResponseEntity.badRequest().body("Error al crear la retención: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            return retencionService.obtenerPorId(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error al obtener retención con id: {}", id, e);
            return ResponseEntity.badRequest().body("Error al obtener la retención: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody Retencion retencion) {
        try {
            retencion.setId(id);
            Retencion updated = retencionService.guardar(retencion);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error al actualizar retención con id: {}", id, e);
            return ResponseEntity.badRequest().body("Error al actualizar la retención: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        try {
            System.out.println("=== DEBUG ELIMINAR RETENCION ===");
            System.out.println("ID a eliminar: " + id);
            retencionService.eliminar(id);
            System.out.println("Retención eliminada exitosamente");
            System.out.println("===============================");
            return ResponseEntity.ok("Retención eliminada exitosamente");
        } catch (IllegalArgumentException e) {
            System.out.println("Error al eliminar retención: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error inesperado al eliminar: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("No se puede eliminar la retención. Puede estar siendo utilizada en otras operaciones.");
        }
    }
    
    @PostMapping("/{id}/cambiar-estado")
    @ResponseBody
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        try {
            Retencion.EstadoRetencion nuevoEstado = Retencion.EstadoRetencion.valueOf(estado.toUpperCase());
            Optional<Retencion> retencionOpt = retencionService.obtenerPorId(id);
            
            if (retencionOpt.isPresent()) {
                Retencion retencion = retencionOpt.get();
                retencion.setEstado(nuevoEstado);
                retencionService.guardar(retencion);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error al cambiar estado de retención con id: {}", id, e);
            return ResponseEntity.badRequest().body("Error al cambiar el estado: " + e.getMessage());
        }
    }
    
    @GetMapping("/buscar")
    @ResponseBody
    public ResponseEntity<?> buscar(@RequestParam(required = false) String termino,
                                   @RequestParam(required = false) String estado) {
        try {
            List<Retencion> resultados;
            
            if (termino != null && !termino.trim().isEmpty()) {
                // Buscar por término (implementar búsqueda por contribuyente)
                resultados = retencionService.obtenerTodas().stream()
                        .filter(r -> r.getContribuyente() != null && 
                                   r.getContribuyente().getRazonSocial().toLowerCase().contains(termino.toLowerCase()))
                        .toList();
            } else if (estado != null && !estado.trim().isEmpty()) {
                Retencion.EstadoRetencion estadoEnum = Retencion.EstadoRetencion.valueOf(estado.toUpperCase());
                resultados = retencionService.obtenerPorEstado(estadoEnum);
            } else {
                resultados = retencionService.obtenerTodas();
            }
            
            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            log.error("Error al buscar retenciones", e);
            return ResponseEntity.badRequest().body("Error al buscar retenciones: " + e.getMessage());
        }
    }
    
    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar() {
        try {
            List<Retencion> retenciones = retencionService.obtenerTodas();
            
            // Crear workbook de Excel
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Retenciones");
            
            // Estilo para el encabezado
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // Bordes para el encabezado
            headerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            
            // Crear encabezados
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Contribuyente", "RUC", "Concepto", "Fecha", "Porcentaje (%)", "Monto Base", "Monto Retenido", "Estado", "Creado En"};
            
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Llenar datos
            int rowNum = 1;
            for (Retencion retencion : retenciones) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(retencion.getId());
                row.createCell(1).setCellValue(retencion.getContribuyente() != null ? retencion.getContribuyente().getRazonSocial() : "");
                row.createCell(2).setCellValue(retencion.getContribuyente() != null ? retencion.getContribuyente().getRif() : "");
                row.createCell(3).setCellValue(retencion.getConcepto());
                row.createCell(4).setCellValue(retencion.getFecha().toString());
                row.createCell(5).setCellValue(retencion.getPorcentaje().doubleValue());
                row.createCell(6).setCellValue(retencion.getMontoBase().doubleValue());
                row.createCell(7).setCellValue(retencion.getMontoRetenido().doubleValue());
                row.createCell(8).setCellValue(retencion.getEstado().getDescripcion());
                row.createCell(9).setCellValue(retencion.getCreadoEn().toString());
            }
            
            // Auto-ajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Convertir a bytes
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .header("Content-Disposition", "attachment; filename=\"retenciones_" + 
                           java.time.LocalDate.now().toString() + ".xlsx\"")
                    .body(outputStream.toByteArray());
                    
        } catch (Exception e) {
            log.error("Error al exportar retenciones", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}/print")
    public ResponseEntity<?> imprimirComprobante(@PathVariable Long id) {
        try {
            // Implementar generación de PDF del comprobante de retención
            return ResponseEntity.ok("Generación de PDF no implementada aún");
        } catch (Exception e) {
            log.error("Error al generar comprobante de retención con id: {}", id, e);
            return ResponseEntity.badRequest().body("Error al generar comprobante: " + e.getMessage());
        }
    }
    
    @GetMapping("/estadisticas")
    @ResponseBody
    public ResponseEntity<?> obtenerEstadisticas() {
        try {
            return ResponseEntity.ok(obtenerEstadisticasPrivadas());
        } catch (Exception e) {
            log.error("Error al obtener estadísticas", e);
            return ResponseEntity.badRequest().body("Error al obtener estadísticas: " + e.getMessage());
        }
    }
    
    private Map<String, Object> obtenerEstadisticasPrivadas() {
        try {
            Map<String, Object> estadisticas = new HashMap<>();
            
            List<Retencion> todas = retencionService.obtenerTodas();
            
            // Contar por estado
            long pendientes = todas.stream().filter(r -> r.getEstado() == Retencion.EstadoRetencion.PENDIENTE).count();
            long aplicadas = todas.stream().filter(r -> r.getEstado() == Retencion.EstadoRetencion.APLICADA).count();
            long anuladas = todas.stream().filter(r -> r.getEstado() == Retencion.EstadoRetencion.ANULADA).count();
            
            // Calcular totales
            BigDecimal totalPendiente = todas.stream()
                    .filter(r -> r.getEstado() == Retencion.EstadoRetencion.PENDIENTE)
                    .map(Retencion::getMontoRetenido)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalAplicado = retencionService.obtenerMontoTotalAplicado();
            BigDecimal montoTotal = totalPendiente.add(totalAplicado);
            
            // Contar este mes
            long esteMes = todas.stream()
                    .filter(r -> r.getFecha().getMonth() == java.time.LocalDate.now().getMonth() &&
                               r.getFecha().getYear() == java.time.LocalDate.now().getYear())
                    .count();
            
            estadisticas.put("total", todas.size());
            estadisticas.put("esteMes", esteMes);
            estadisticas.put("montoTotal", montoTotal);
            estadisticas.put("pendientes", pendientes);
            estadisticas.put("aplicadas", aplicadas);
            estadisticas.put("anuladas", anuladas);
            estadisticas.put("totalPendiente", totalPendiente);
            estadisticas.put("totalAplicado", totalAplicado);
            
            return estadisticas;
        } catch (Exception e) {
            log.error("Error al obtener estadísticas", e);
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("total", 0);
            defaultStats.put("esteMes", 0);
            defaultStats.put("montoTotal", BigDecimal.ZERO);
            return defaultStats;
        }
    }
}
