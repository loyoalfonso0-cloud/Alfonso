package com.example.demo.personal.web;

import com.example.demo.personal.model.Personal;
import com.example.demo.personal.service.PersonalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

// Imports para Apache POI (Excel) - se usan con nombres completos para evitar conflictos

@Controller
@RequiredArgsConstructor
public class PersonalController {

    private final PersonalService personalService;

    @GetMapping("/personal")
    @PreAuthorize("hasAnyAuthority('PERSONAL_READ','PERSONAL_VER','PERSONAL_GESTIONAR') or hasRole('ADMIN_TRIBUTARIO')")
    public String index(Model model) {
        model.addAttribute("lista", personalService.obtenerTodos());
        model.addAttribute("estadisticas", personalService.obtenerEstadisticas());
        return "personal/index";
    }

    @GetMapping("/personal/roles")
    @ResponseBody
    public ResponseEntity<?> obtenerRoles() {
        try {
            return ResponseEntity.ok(personalService.obtenerRoles());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener roles: " + e.getMessage());
        }
    }

    // API REST Endpoints

    @GetMapping("/personal/api")
    @ResponseBody
    public List<Personal> listarTodos() {
        return personalService.obtenerTodos();
    }

    @GetMapping("/personal/api/{id}")
    @ResponseBody
    public ResponseEntity<Personal> obtenerPorId(@PathVariable Long id) {
        return personalService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/personal")
    @ResponseBody
    public ResponseEntity<?> crear(@Valid @ModelAttribute Personal personal,
                                   BindingResult result,
                                   @RequestParam(required = false) String email,
                                   @RequestParam(required = false) String password,
                                   @RequestParam(required = false) Long rolId) {
        try {
            if (result.hasErrors()) {
                return ResponseEntity.badRequest().body("Datos inválidos: " + result.getAllErrors());
            }

            Personal nuevoPersonal = personalService.crear(personal, email, password, rolId);
            return ResponseEntity.ok(nuevoPersonal);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/personal/{id}")
    @ResponseBody
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @ModelAttribute Personal personal,
                                        BindingResult result) {
        try {
            if (result.hasErrors()) {
                return ResponseEntity.badRequest().body("Datos inválidos: " + result.getAllErrors());
            }

            Personal personalActualizado = personalService.actualizar(id, personal);
            return ResponseEntity.ok(personalActualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/personal/{id}/estado")
    @ResponseBody
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id,
                                           @RequestParam boolean activo) {
        try {
            Personal personal = personalService.cambiarEstado(id, activo);
            return ResponseEntity.ok(personal);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/personal/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            personalService.eliminar(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/personal/buscar")
    @ResponseBody
    public List<Personal> buscar(@RequestParam String termino) {
        return personalService.buscarPorTermino(termino);
    }

    @GetMapping("/personal/estadisticas")
    @ResponseBody
    public Map<String, Object> obtenerEstadisticas() {
        return personalService.obtenerEstadisticas();
    }

    @PutMapping("/personal/{id}/rol")
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('USUARIOS_EDITAR','PERSONAL_GESTIONAR')")
    public ResponseEntity<?> asignarRol(@PathVariable Long id,
                                        @RequestParam Long rolId) {
        try {
            Personal personal = personalService.asignarRol(id, rolId);
            return ResponseEntity.ok(personal);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/personal/{id}/rol")
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('USUARIOS_EDITAR','PERSONAL_GESTIONAR')")
    public ResponseEntity<?> removerRol(@PathVariable Long id) {
        try {
            Personal personal = personalService.removerRol(id);
            return ResponseEntity.ok(personal);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/personal/export")
    public ResponseEntity<byte[]> exportarExcel() {
        try {
            List<Personal> personal = personalService.obtenerTodos();
            
            // Crear workbook de Excel
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Personal");
            
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
            String[] headers = {"ID", "Nombres", "Apellidos", "Tipo Doc", "Documento", "Email", "Teléfono", "Cargo", "Departamento", "Fecha Ingreso", "Salario", "Estado", "Email Usuario"};
            
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Llenar datos
            int rowNum = 1;
            for (Personal p : personal) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getNombres());
                row.createCell(2).setCellValue(p.getApellidos());
                row.createCell(3).setCellValue(p.getTipoDocumento() != null ? p.getTipoDocumento() : "");
                row.createCell(4).setCellValue(p.getDocumento());
                row.createCell(5).setCellValue(p.getEmail() != null ? p.getEmail() : "");
                row.createCell(6).setCellValue(p.getTelefono() != null ? p.getTelefono() : "");
                row.createCell(7).setCellValue(p.getCargo());
                row.createCell(8).setCellValue(p.getDepartamento() != null ? p.getDepartamento() : "");
                row.createCell(9).setCellValue(p.getFechaIngreso() != null ? p.getFechaIngreso().toString() : "");
                row.createCell(10).setCellValue(p.getSalario() != null ? p.getSalario().doubleValue() : 0.0);
                row.createCell(11).setCellValue(p.isActivo() ? "Activo" : "Inactivo");
                
                String emailUsuario = p.getUsuario() != null ? p.getUsuario().getEmail() : "Sin usuario";
                row.createCell(12).setCellValue(emailUsuario);
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
                    .header("Content-Disposition", "attachment; filename=\"personal_" + 
                           java.time.LocalDate.now().toString() + ".xlsx\"")
                    .body(outputStream.toByteArray());
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}


