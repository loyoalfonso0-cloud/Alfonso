package com.example.demo.tributario.service;

import com.example.demo.tributario.model.*;
import com.example.demo.tributario.repository.*;
import com.example.demo.pagos.model.Pago;
import com.example.demo.pagos.model.MetodoPago;
import com.example.demo.pagos.model.EstadoPago;
import com.example.demo.pagos.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TributarioService {
    private final ContribuyenteRepository contribuyenteRepository;
    private final ImpuestoRepository impuestoRepository;
    private final DeclaracionRepository declaracionRepository;
    private final PagoRepository pagoRepository;

    public List<Contribuyente> listarContribuyentes(){ return contribuyenteRepository.findByActivoTrue(); }
    public List<Impuesto> listarImpuestos(){ return impuestoRepository.findAll(); }
    public List<Declaracion> listarDeclaraciones(){ return declaracionRepository.findAll(); }
    
    // Métodos temporales para retenciones y comprobantes (datos de ejemplo)
    public List<Object> listarRetenciones(){ 
        return List.of(); // Retorna lista vacía por ahora
    }
    
    public List<Object> listarComprobantes(){ 
        return List.of(); // Retorna lista vacía por ahora
    }

    @Transactional
    public Declaracion crearDeclaracion(Long contribuyenteId, Long impuestoId, String periodo, BigDecimal base, BigDecimal monto){
        Contribuyente c = contribuyenteRepository.findById(contribuyenteId).orElseThrow();
        Impuesto i = impuestoRepository.findById(impuestoId).orElseThrow();
        Declaracion d = Declaracion.builder()
                .contribuyente(c)
                .impuesto(i)
                .periodo(periodo)
                .baseImponible(base)
                .monto(monto)
                .estado("PENDIENTE")
                .build();
        return declaracionRepository.save(d);
    }

    public Declaracion obtenerDeclaracionPorId(Long id) {
        return declaracionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Declaración no encontrada con ID: " + id));
    }

    @Transactional
    public Declaracion actualizarDeclaracion(Long id, Long contribuyenteId, Long impuestoId, String periodo, BigDecimal baseImponible, BigDecimal monto) {
        Declaracion declaracion = obtenerDeclaracionPorId(id);
        Contribuyente contribuyente = contribuyenteRepository.findById(contribuyenteId)
                .orElseThrow(() -> new IllegalArgumentException("Contribuyente no encontrado con ID: " + contribuyenteId));
        Impuesto impuesto = impuestoRepository.findById(impuestoId)
                .orElseThrow(() -> new IllegalArgumentException("Impuesto no encontrado con ID: " + impuestoId));
        
        declaracion.setContribuyente(contribuyente);
        declaracion.setImpuesto(impuesto);
        declaracion.setPeriodo(periodo);
        declaracion.setBaseImponible(baseImponible);
        declaracion.setMonto(monto);
        
        return declaracionRepository.save(declaracion);
    }

    @Transactional
    public void eliminarDeclaracion(Long id) {
        if (!declaracionRepository.existsById(id)) {
            throw new IllegalArgumentException("Declaración no encontrada con ID: " + id);
        }
        
        try {
            declaracionRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("No se puede eliminar la declaración porque está siendo utilizada en pagos u otras operaciones.");
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al eliminar la declaración: " + e.getMessage());
        }
    }

    @Transactional
    public Declaracion cambiarEstadoDeclaracion(Long id, String nuevoEstado) {
        Declaracion declaracion = obtenerDeclaracionPorId(id);
        declaracion.setEstado(nuevoEstado);
        return declaracionRepository.save(declaracion);
    }

    public void exportarDeclaracionesExcel(HttpServletResponse response) throws IOException {
        try {
            List<Declaracion> declaraciones = declaracionRepository.findAll();
            
            // Crear workbook de Excel
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Declaraciones");
            
            // Estilo para el encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // Bordes para el encabezado
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            
            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Contribuyente", "RUC", "Impuesto", "Período", "Base Imponible", "Monto", "Estado", "Fecha Creación"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Estilo para las celdas de datos
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            
            // Llenar datos
            int rowNum = 1;
            for (Declaracion declaracion : declaraciones) {
                Row row = sheet.createRow(rowNum++);
                
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(declaracion.getId());
                cell0.setCellStyle(dataStyle);
                
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(declaracion.getContribuyente().getRazonSocial());
                cell1.setCellStyle(dataStyle);
                
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(declaracion.getContribuyente().getRif());
                cell2.setCellStyle(dataStyle);
                
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(declaracion.getImpuesto().getNombre());
                cell3.setCellStyle(dataStyle);
                
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(declaracion.getPeriodo());
                cell4.setCellStyle(dataStyle);
                
                Cell cell5 = row.createCell(5);
                cell5.setCellValue(declaracion.getBaseImponible().doubleValue());
                cell5.setCellStyle(dataStyle);
                
                Cell cell6 = row.createCell(6);
                cell6.setCellValue(declaracion.getMonto().doubleValue());
                cell6.setCellStyle(dataStyle);
                
                Cell cell7 = row.createCell(7);
                cell7.setCellValue(declaracion.getEstado());
                cell7.setCellStyle(dataStyle);
                
                Cell cell8 = row.createCell(8);
                cell8.setCellValue(declaracion.getCreadoEn().toString());
                cell8.setCellStyle(dataStyle);
            }
            
            // Auto-ajustar el ancho de las columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Configurar respuesta HTTP
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"declaraciones.xlsx\"");
            
            // Escribir el archivo Excel a la respuesta
            workbook.write(response.getOutputStream());
            workbook.close();
            
        } catch (Exception e) {
            throw new IOException("Error al generar archivo Excel", e);
        }
    }

    @Transactional
    public Pago registrarPago(Long declaracionId, BigDecimal monto, String metodo, String referencia){
        Declaracion d = declaracionRepository.findById(declaracionId)
                .orElseThrow(() -> new IllegalArgumentException("Declaración no encontrada con ID: " + declaracionId));
        
        // Validar que el monto sea positivo
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a cero");
        }
        
        // Validar que el método de pago no esté vacío
        if (metodo == null || metodo.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe especificar un método de pago");
        }
        
        // Convertir string a enum MetodoPago
        MetodoPago metodoPagoEnum;
        try {
            metodoPagoEnum = MetodoPago.valueOf(metodo.toUpperCase());
        } catch (IllegalArgumentException e) {
            metodoPagoEnum = MetodoPago.TRANSFERENCIA; // Valor por defecto
        }
        
        Pago p = Pago.builder()
                .contribuyente(d.getContribuyente())
                .declaracion(d)
                .fechaPago(LocalDateTime.now())
                .monto(monto)
                .metodoPago(metodoPagoEnum)
                .referencia(referencia != null ? referencia : "")
                .estado(EstadoPago.PENDIENTE)
                .concepto("Pago de declaración " + d.getPeriodo())
                .build();
        
        // Cambiar estado de la declaración a PAGADA
        d.setEstado("PAGADA");
        declaracionRepository.save(d);
        return pagoRepository.save(p);
    }
}


