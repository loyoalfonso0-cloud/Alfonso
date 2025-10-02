package com.example.demo.tributario.web;

import com.example.demo.tributario.model.Comprobante;
import com.example.demo.tributario.service.ComprobanteService;
import com.example.demo.tributario.service.ContribuyenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/tributario/comprobantes")
@RequiredArgsConstructor
@Slf4j
public class ComprobanteController {
    
    private final ComprobanteService comprobanteService;
    private final ContribuyenteService contribuyenteService;
    
    @GetMapping
    public String listar(Model model) {
        try {
            model.addAttribute("comprobantes", comprobanteService.listarTodos());
            model.addAttribute("contribuyentes", contribuyenteService.listarActivos());
            model.addAttribute("estadisticas", comprobanteService.obtenerEstadisticas());
            return "tributario/comprobantes";
        } catch (Exception e) {
            log.error("Error al cargar comprobantes", e);
            model.addAttribute("error", "Error al cargar los comprobantes: " + e.getMessage());
            return "tributario/comprobantes";
        }
    }
    
    @PostMapping
    @ResponseBody
    public ResponseEntity<?> crear(@Valid @RequestBody Comprobante comprobante) {
        try {
            Comprobante saved = comprobanteService.crear(comprobante);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("Error al crear comprobante", e);
            return ResponseEntity.badRequest().body("Error al crear el comprobante: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            return comprobanteService.buscarPorId(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error al obtener comprobante con id: {}", id, e);
            return ResponseEntity.badRequest().body("Error al obtener el comprobante: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody Comprobante comprobante) {
        try {
            Comprobante updated = comprobanteService.actualizar(id, comprobante);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error al actualizar comprobante con id: {}", id, e);
            return ResponseEntity.badRequest().body("Error al actualizar el comprobante: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        try {
            System.out.println("=== DEBUG ELIMINAR COMPROBANTE ===");
            System.out.println("ID a eliminar: " + id);
            comprobanteService.eliminar(id);
            System.out.println("Comprobante eliminado exitosamente");
            System.out.println("===============================");
            return ResponseEntity.ok("Comprobante eliminado exitosamente");
        } catch (IllegalArgumentException e) {
            System.out.println("Error al eliminar comprobante: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error inesperado al eliminar: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("No se puede eliminar el comprobante. Puede estar siendo utilizado en otras operaciones.");
        }
    }
    
    @GetMapping("/buscar")
    @ResponseBody
    public ResponseEntity<?> buscar(@RequestParam(required = false) String termino,
                                   @RequestParam(required = false) String tipo,
                                   @RequestParam(required = false) String estado) {
        try {
            List<Comprobante> resultados = comprobanteService.listarTodos();
            
            if (termino != null && !termino.trim().isEmpty()) {
                resultados = resultados.stream()
                        .filter(c -> c.getNumero().toLowerCase().contains(termino.toLowerCase()) ||
                                   (c.getContribuyente() != null && 
                                    c.getContribuyente().getRazonSocial().toLowerCase().contains(termino.toLowerCase())))
                        .toList();
            }
            
            if (tipo != null && !tipo.trim().isEmpty()) {
                resultados = resultados.stream()
                        .filter(c -> c.getTipo().equals(tipo))
                        .toList();
            }
            
            if (estado != null && !estado.trim().isEmpty()) {
                try {
                    Comprobante.EstadoComprobante estadoEnum = Comprobante.EstadoComprobante.valueOf(estado.toUpperCase());
                    resultados = resultados.stream()
                            .filter(c -> c.getEstado().equals(estadoEnum))
                            .toList();
                } catch (IllegalArgumentException e) {
                    log.warn("Estado inválido: {}", estado);
                }
            }
            
            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            log.error("Error al buscar comprobantes", e);
            return ResponseEntity.badRequest().body("Error al buscar comprobantes: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}/estado")
    @ResponseBody
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestBody java.util.Map<String, String> request) {
        try {
            String estado = request.get("estado");
            Comprobante.EstadoComprobante nuevoEstado = Comprobante.EstadoComprobante.valueOf(estado.toUpperCase());
            Comprobante comprobante = comprobanteService.cambiarEstado(id, nuevoEstado);
            return ResponseEntity.ok(comprobante);
        } catch (Exception e) {
            log.error("Error al cambiar estado del comprobante con id: {}", id, e);
            return ResponseEntity.badRequest().body("Error al cambiar el estado: " + e.getMessage());
        }
    }
    
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportar() {
        try {
            List<Comprobante> comprobantes = comprobanteService.listarTodos();
            
            // Crear workbook de Excel
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Comprobantes");
            
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
            String[] headers = {"Número", "Serie", "Tipo", "Contribuyente", "RUC", "Fecha Emisión", "Subtotal", "Impuesto", "Total", "Estado"};
            
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Llenar datos
            int rowNum = 1;
            for (Comprobante comprobante : comprobantes) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(comprobante.getNumero());
                row.createCell(1).setCellValue(comprobante.getSerie());
                row.createCell(2).setCellValue(comprobante.getTipo().toString());
                row.createCell(3).setCellValue(comprobante.getContribuyente() != null ? comprobante.getContribuyente().getRazonSocial() : "");
                row.createCell(4).setCellValue(comprobante.getContribuyente() != null ? comprobante.getContribuyente().getRif() : "");
                row.createCell(5).setCellValue(comprobante.getFechaEmision().toString());
                row.createCell(6).setCellValue(comprobante.getSubtotal().doubleValue());
                row.createCell(7).setCellValue(comprobante.getImpuesto().doubleValue());
                row.createCell(8).setCellValue(comprobante.getTotal().doubleValue());
                row.createCell(9).setCellValue(comprobante.getEstado().getDescripcion());
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
                    .header("Content-Disposition", "attachment; filename=\"comprobantes_" + 
                           java.time.LocalDate.now().toString() + ".xlsx\"")
                    .body(outputStream.toByteArray());
                    
        } catch (Exception e) {
            log.error("Error al exportar comprobantes", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/cambiar-estado")
    @ResponseBody
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        try {
            Comprobante.EstadoComprobante nuevoEstado = Comprobante.EstadoComprobante.valueOf(estado.toUpperCase());
            Comprobante comprobante = comprobanteService.cambiarEstado(id, nuevoEstado);
            return ResponseEntity.ok(comprobante);
        } catch (Exception e) {
            log.error("Error al cambiar estado del comprobante con id: {}", id, e);
            return ResponseEntity.badRequest().body("Error al cambiar el estado: " + e.getMessage());
        }
    }
}
