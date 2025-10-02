package com.example.demo.tributario.web;

import com.example.demo.tributario.model.Impuesto;
import com.example.demo.tributario.service.ImpuestoService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Controller
@RequestMapping("/tributario/impuestos")
@RequiredArgsConstructor
public class ImpuestoController {

    private final ImpuestoService impuestoService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('IMPUESTOS_READ','IMPUESTOS_VER','IMPUESTOS_GESTIONAR') or hasRole('ADMIN_TRIBUTARIO')")
    public String listar(Model model) {
        model.addAttribute("impuestos", impuestoService.listarTodos());
        model.addAttribute("estadisticas", impuestoService.obtenerEstadisticas());
        return "tributario/impuestos";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('IMPUESTOS_WRITE','IMPUESTOS_GESTIONAR') or hasRole('ADMIN_TRIBUTARIO')")
    public String crear(@Valid @ModelAttribute Impuesto impuesto, 
                       BindingResult result, 
                       RedirectAttributes redirectAttributes) {
        
        // Log para depuración
        System.out.println("=== DEBUG CREAR IMPUESTO ===");
        System.out.println("Código: " + impuesto.getCodigo());
        System.out.println("Nombre: " + impuesto.getNombre());
        System.out.println("Tasa: " + impuesto.getTasa());
        System.out.println("Errores de validación: " + result.hasErrors());
        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> System.out.println("Error: " + error.getDefaultMessage()));
        }
        System.out.println("===============================");
        
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Error en los datos del formulario");
            return "redirect:/tributario/impuestos";
        }

        try {
            Impuesto saved = impuestoService.crear(impuesto);
            System.out.println("Impuesto guardado con ID: " + saved.getId());
            redirectAttributes.addFlashAttribute("success", "Impuesto creado exitosamente");
        } catch (IllegalArgumentException e) {
            System.out.println("Error al crear impuesto: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            System.out.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error inesperado al crear el impuesto");
        }

        return "redirect:/tributario/impuestos";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Impuesto> obtenerPorId(@PathVariable Long id) {
        return impuestoService.buscarPorId(id)
            .map(impuesto -> ResponseEntity.ok(impuesto))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('IMPUESTOS_WRITE','IMPUESTOS_GESTIONAR') or hasRole('ADMIN_TRIBUTARIO')")
    public ResponseEntity<String> actualizar(@PathVariable Long id, 
                                           @Valid @ModelAttribute Impuesto impuesto,
                                           BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Error en los datos del formulario");
        }

        try {
            impuestoService.actualizar(id, impuesto);
            return ResponseEntity.ok("Impuesto actualizado exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('IMPUESTOS_DELETE','IMPUESTOS_GESTIONAR') or hasRole('ADMIN_TRIBUTARIO')")
    @ResponseBody
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        try {
            System.out.println("=== DEBUG ELIMINAR IMPUESTO ===");
            System.out.println("ID a eliminar: " + id);
            impuestoService.eliminar(id);
            System.out.println("Impuesto eliminado exitosamente");
            System.out.println("===============================");
            return ResponseEntity.ok("Impuesto eliminado exitosamente");
        } catch (IllegalArgumentException e) {
            System.out.println("Error al eliminar impuesto: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error inesperado al eliminar: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("No se puede eliminar el impuesto. Puede estar siendo utilizado en otras operaciones.");
        }
    }

    @GetMapping("/export")
    public void exportarExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"impuestos.xlsx\"");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Impuestos");
            
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
            String[] headers = {"ID", "Código", "Nombre", "Tasa (%)"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Llenar datos
            List<Impuesto> impuestos = impuestoService.listarTodos();
            int rowNum = 1;
            
            for (Impuesto i : impuestos) {
                Row row = sheet.createRow(rowNum++);
                
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(i.getId());
                cell0.setCellStyle(dataStyle);
                
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(i.getCodigo());
                cell1.setCellStyle(dataStyle);
                
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(i.getNombre());
                cell2.setCellStyle(dataStyle);
                
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(i.getTasa().doubleValue());
                cell3.setCellStyle(dataStyle);
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
        return ResponseEntity.ok(impuestoService.obtenerEstadisticas());
    }
}
