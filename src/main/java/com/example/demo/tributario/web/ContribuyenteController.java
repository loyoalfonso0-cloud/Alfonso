package com.example.demo.tributario.web;

import com.example.demo.tributario.model.Contribuyente;
import com.example.demo.tributario.service.ContribuyenteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;

@Controller
@RequestMapping("/tributario/contribuyentes")
@RequiredArgsConstructor
public class ContribuyenteController {

    private final ContribuyenteService contribuyenteService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('CONTRIBUYENTES_READ','CONTRIBUYENTES_VER','CONTRIBUYENTES_GESTIONAR') or hasRole('ADMIN_TRIBUTARIO')")
    public String listar(Model model) {
        model.addAttribute("contribuyentes", contribuyenteService.listarTodos());
        model.addAttribute("estadisticas", contribuyenteService.obtenerEstadisticas());
        return "tributario/contribuyentes";
    }

    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<List<Contribuyente>> listarAPI() {
        return ResponseEntity.ok(contribuyenteService.listarActivos());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('CONTRIBUYENTES_WRITE','CONTRIBUYENTES_GESTIONAR') or hasRole('ADMIN_TRIBUTARIO')")
    public String crear(@Valid @ModelAttribute Contribuyente contribuyente, 
                       BindingResult result, 
                       RedirectAttributes redirectAttributes) {
        
        // Log para depuración
        System.out.println("=== DEBUG CREAR CONTRIBUYENTE ===");
        System.out.println("RIF: " + contribuyente.getRif());
        System.out.println("Razón Social: " + contribuyente.getRazonSocial());
        System.out.println("Email: " + contribuyente.getEmail());
        System.out.println("Dirección: " + contribuyente.getDireccion());
        System.out.println("Tipo: " + contribuyente.getTipoContribuyente());
        System.out.println("Activo: " + contribuyente.getActivo());
        System.out.println("Errores de validación: " + result.hasErrors());
        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> {
                System.out.println("Error: " + error.getDefaultMessage());
                System.out.println("Campo: " + error.getObjectName());
                if (error instanceof org.springframework.validation.FieldError) {
                    org.springframework.validation.FieldError fieldError = (org.springframework.validation.FieldError) error;
                    System.out.println("Campo específico: " + fieldError.getField());
                    System.out.println("Valor rechazado: " + fieldError.getRejectedValue());
                }
            });
        }
        System.out.println("================================");
        
        // Validaciones adicionales del lado del servidor
        if (contribuyente.getRif() == null || contribuyente.getRif().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El RIF es obligatorio");
            return "redirect:/tributario/contribuyentes";
        }
        
        // Validar según el tipo de contribuyente
        if (contribuyente.getTipoContribuyente() == Contribuyente.TipoContribuyente.PERSONA_NATURAL) {
            // Para persona natural: nombre y apellido son obligatorios
            if (contribuyente.getNombre() == null || contribuyente.getNombre().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El nombre es obligatorio para Persona Natural");
                return "redirect:/tributario/contribuyentes";
            }
            if (contribuyente.getApellido() == null || contribuyente.getApellido().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El apellido es obligatorio para Persona Natural");
                return "redirect:/tributario/contribuyentes";
            }
            // Generar razón social automáticamente para persona natural
            contribuyente.setRazonSocial(contribuyente.getNombre() + " " + contribuyente.getApellido());
        } else {
            // Para persona jurídica: razón social es obligatoria
            if (contribuyente.getRazonSocial() == null || contribuyente.getRazonSocial().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "La razón social es obligatoria para Persona Jurídica");
                return "redirect:/tributario/contribuyentes";
            }
        }
        
        if (contribuyente.getDireccion() == null || contribuyente.getDireccion().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "La dirección es obligatoria");
            return "redirect:/tributario/contribuyentes";
        }
        
        if (contribuyente.getEmail() == null || contribuyente.getEmail().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El email es obligatorio");
            return "redirect:/tributario/contribuyentes";
        }
        
        if (contribuyente.getTipoContribuyente() == null) {
            redirectAttributes.addFlashAttribute("error", "El tipo de contribuyente es obligatorio");
            return "redirect:/tributario/contribuyentes";
        }
        
        // Establecer valores por defecto si son nulos
        if (contribuyente.getActivo() == null) {
            contribuyente.setActivo(true);
        }
        
        if (contribuyente.getCreadoEn() == null) {
            contribuyente.setCreadoEn(java.time.LocalDateTime.now());
        }
        
        if (result.hasErrors()) {
            StringBuilder errorMsg = new StringBuilder("Errores de validación: ");
            result.getAllErrors().forEach(error -> errorMsg.append(error.getDefaultMessage()).append("; "));
            redirectAttributes.addFlashAttribute("error", errorMsg.toString());
            return "redirect:/tributario/contribuyentes";
        }

        try {
            Contribuyente saved = contribuyenteService.crear(contribuyente);
            System.out.println("Contribuyente guardado con ID: " + saved.getId());
            redirectAttributes.addFlashAttribute("success", "Contribuyente creado exitosamente");
        } catch (IllegalArgumentException e) {
            System.out.println("Error al crear contribuyente: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            System.out.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error inesperado al crear el contribuyente: " + e.getMessage());
        }

        return "redirect:/tributario/contribuyentes";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Contribuyente> obtenerPorId(@PathVariable Long id) {
        return contribuyenteService.buscarPorId(id)
            .map(contribuyente -> ResponseEntity.ok(contribuyente))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CONTRIBUYENTES_WRITE','CONTRIBUYENTES_GESTIONAR') or hasRole('ADMIN_TRIBUTARIO')")
    public ResponseEntity<String> actualizar(@PathVariable Long id, 
                                           @Valid @ModelAttribute Contribuyente contribuyente,
                                           BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Error en los datos del formulario");
        }

        try {
            contribuyenteService.actualizar(id, contribuyente);
            return ResponseEntity.ok("Contribuyente actualizado exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyAuthority('CONTRIBUYENTES_WRITE','CONTRIBUYENTES_GESTIONAR') or hasRole('ADMIN_TRIBUTARIO')")
    @ResponseBody
    public ResponseEntity<String> cambiarEstado(@PathVariable Long id, 
                                              @RequestBody Map<String, Boolean> request) {
        try {
            Boolean nuevoEstado = request.get("activo");
            contribuyenteService.cambiarEstado(id, nuevoEstado);
            return ResponseEntity.ok("Estado actualizado exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/buscar")
    @ResponseBody
    public ResponseEntity<Object> buscar(@RequestParam(required = false) String termino,
                                        @RequestParam(required = false) String tipo) {
        try {
            if (termino != null && !termino.trim().isEmpty()) {
                return ResponseEntity.ok(contribuyenteService.buscarPorTermino(termino));
            } else if (tipo != null && !tipo.trim().isEmpty()) {
                Contribuyente.TipoContribuyente tipoEnum = Contribuyente.TipoContribuyente.valueOf(tipo);
                return ResponseEntity.ok(contribuyenteService.listarPorTipo(tipoEnum));
            } else {
                return ResponseEntity.ok(contribuyenteService.listarTodos());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error en la búsqueda: " + e.getMessage());
        }
    }

    @GetMapping("/export")
    public void exportarExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"contribuyentes.xlsx\"");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Contribuyentes");
            
            // Crear estilo para headers
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            
            // Crear estilo para datos
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            
            // Crear fila de headers
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "RUC", "Razón Social", "Tipo", "Dirección", "Email", "Teléfono", "Representante Legal", "Estado", "Fecha Creación"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Llenar datos
            List<Contribuyente> contribuyentes = contribuyenteService.listarTodos();
            int rowNum = 1;
            
            for (Contribuyente c : contribuyentes) {
                Row row = sheet.createRow(rowNum++);
                
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(c.getId());
                cell0.setCellStyle(dataStyle);
                
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(c.getRif());
                cell1.setCellStyle(dataStyle);
                
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(c.getRazonSocial());
                cell2.setCellStyle(dataStyle);
                
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(c.getTipoContribuyente().getDescripcion());
                cell3.setCellStyle(dataStyle);
                
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(c.getDireccion() != null ? c.getDireccion() : "");
                cell4.setCellStyle(dataStyle);
                
                Cell cell5 = row.createCell(5);
                cell5.setCellValue(c.getEmail() != null ? c.getEmail() : "");
                cell5.setCellStyle(dataStyle);
                
                Cell cell6 = row.createCell(6);
                cell6.setCellValue(c.getTelefono() != null ? c.getTelefono() : "");
                cell6.setCellStyle(dataStyle);
                
                Cell cell7 = row.createCell(7);
                cell7.setCellValue(c.getRepresentanteLegal() != null ? c.getRepresentanteLegal() : "");
                cell7.setCellStyle(dataStyle);
                
                Cell cell8 = row.createCell(8);
                cell8.setCellValue(c.getActivo() ? "Activo" : "Inactivo");
                cell8.setCellStyle(dataStyle);
                
                Cell cell9 = row.createCell(9);
                cell9.setCellValue(c.getCreadoEn() != null ? c.getCreadoEn().toString() : "");
                cell9.setCellStyle(dataStyle);
            }
            
            // Ajustar ancho de columnas automáticamente
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Agregar un poco de padding extra
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, currentWidth + 1000);
            }
            
            workbook.write(response.getOutputStream());
        }
    }

    @GetMapping("/estadisticas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        return ResponseEntity.ok(contribuyenteService.obtenerEstadisticas());
    }
}
