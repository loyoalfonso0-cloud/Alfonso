package com.example.demo.auditoria.web;

import com.example.demo.auditoria.model.AuditoriaLog;
import com.example.demo.auditoria.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/auditoria")
public class AuditoriaController {
    
    private final AuditoriaService auditoriaService;
    
    // ==================== ENDPOINTS WEB ====================
    
    /**
     * Página principal de auditoría
     */
    @GetMapping
    public String mostrarAuditoria(Model model) {
        try {
            // Obtener estadísticas generales
            Map<String, Object> estadisticas = auditoriaService.obtenerEstadisticasGenerales();
            model.addAttribute("estadisticas", estadisticas);
            
            // Obtener logs recientes (primera página)
            Page<AuditoriaLog> logsRecientes = auditoriaService.obtenerTodosLosLogs(0, 10);
            model.addAttribute("logs", logsRecientes.getContent());
            model.addAttribute("totalPages", logsRecientes.getTotalPages());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalElements", logsRecientes.getTotalElements());
            
            // Obtener logs de errores recientes
            List<AuditoriaLog> errores = auditoriaService.obtenerLogsDeErrores();
            model.addAttribute("erroresRecientes", errores.size() > 10 ? errores.subList(0, 10) : errores);
            
            // NO registrar acceso al módulo - solo acciones CRUD
            
            return "auditoria/auditoria";
            
        } catch (Exception e) {
            log.error("Error al cargar página de auditoría: {}", e.getMessage());
            model.addAttribute("error", "Error al cargar los datos de auditoría");
            return "error";
        }
    }
    
    // ==================== ENDPOINTS REST ====================
    
    /**
     * API para obtener logs con filtros y paginación
     */
    @GetMapping("/api/logs")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerLogs(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String termino,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Page<AuditoriaLog> logs = auditoriaService.obtenerLogsConFiltros(
                usuario, modulo, accion, fechaInicio, fechaFin, ip, termino, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("logs", logs.getContent());
            response.put("totalPages", logs.getTotalPages());
            response.put("totalElements", logs.getTotalElements());
            response.put("currentPage", logs.getNumber());
            response.put("size", logs.getSize());
            response.put("hasNext", logs.hasNext());
            response.put("hasPrevious", logs.hasPrevious());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al obtener logs: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al obtener los logs");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * API para obtener estadísticas
     */
    @GetMapping("/api/estadisticas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        try {
            Map<String, Object> estadisticas = auditoriaService.obtenerEstadisticasGenerales();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            log.error("Error al obtener estadísticas: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al obtener estadísticas");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * API para obtener detalles de un log específico
     */
    @GetMapping("/api/logs/{id}")
    @ResponseBody
    public ResponseEntity<AuditoriaLog> obtenerLogPorId(@PathVariable Long id) {
        try {
            log.info("Solicitando log con ID: {}", id);
            
            // Verificar que el ID no sea nulo
            if (id == null) {
                log.error("ID de log es nulo");
                return ResponseEntity.badRequest().build();
            }
            
            // Obtener el log por ID
            AuditoriaLog auditoriaLog = auditoriaService.obtenerLogPorId(id);
            
            if (auditoriaLog != null) {
                log.info("Log encontrado: {}", auditoriaLog.getId());
                return ResponseEntity.ok(auditoriaLog);
            } else {
                log.warn("Log no encontrado con ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error al obtener log por ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    /**
     * API de prueba para verificar conectividad
     */
    @GetMapping("/api/test")
    @ResponseBody
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API de auditoría funcionando correctamente");
    }
    
    /**
     * Exportar logs a Excel
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportarLogsExcel(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String termino) {
        
        try {
            // Obtener todos los logs que coincidan con los filtros (máximo 10000)
            Page<AuditoriaLog> logs = auditoriaService.obtenerLogsConFiltros(
                usuario, modulo, accion, fechaInicio, fechaFin, ip, termino, 0, 10000);
            
            // Crear archivo Excel
            byte[] excelBytes = crearArchivoExcel(logs.getContent());
            
            // Registrar exportación
            auditoriaService.registrarAccion("EXPORT", "AUDITORIA", 
                String.format("Usuario exportó %d logs a Excel", logs.getContent().size()));
            
            // Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                "auditoria_logs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
                
        } catch (Exception e) {
            log.error("Error al exportar logs: {}", e.getMessage());
            auditoriaService.registrarError("EXPORT", "AUDITORIA", 
                "Error al exportar logs", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    /**
     * Crea un archivo Excel con los logs
     */
    private byte[] crearArchivoExcel(List<AuditoriaLog> logs) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Logs de Auditoría");
            
            // Crear estilo para encabezados
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            
            // Crear estilo para celdas
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            
            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Usuario", "Acción", "Módulo", "Entidad", "Descripción", 
                              "Fecha/Hora", "IP Address", "Resultado"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Llenar datos
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            int rowNum = 1;
            
            for (AuditoriaLog log : logs) {
                Row row = sheet.createRow(rowNum++);
                
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(log.getId());
                cell0.setCellStyle(cellStyle);
                
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(log.getUsuario());
                cell1.setCellStyle(cellStyle);
                
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(log.getAccion());
                cell2.setCellStyle(cellStyle);
                
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(log.getModulo());
                cell3.setCellStyle(cellStyle);
                
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(log.getEntidad() != null ? log.getEntidad() : "");
                cell4.setCellStyle(cellStyle);
                
                Cell cell5 = row.createCell(5);
                cell5.setCellValue(log.getDescripcion() != null ? log.getDescripcion() : "");
                cell5.setCellStyle(cellStyle);
                
                Cell cell6 = row.createCell(6);
                cell6.setCellValue(log.getFechaHora().format(formatter));
                cell6.setCellStyle(cellStyle);
                
                Cell cell7 = row.createCell(7);
                cell7.setCellValue(log.getIpAddress() != null ? log.getIpAddress() : "");
                cell7.setCellStyle(cellStyle);
                
                Cell cell8 = row.createCell(8);
                cell8.setCellValue(log.getResultado() != null ? log.getResultado() : "");
                cell8.setCellStyle(cellStyle);
            }
            
            // Auto-ajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Convertir a bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
