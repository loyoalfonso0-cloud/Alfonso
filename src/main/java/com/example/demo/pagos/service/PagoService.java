package com.example.demo.pagos.service;

import com.example.demo.pagos.dto.PagoRequest;
import com.example.demo.pagos.dto.PagoResponse;
import com.example.demo.pagos.exception.PagoException;
import com.example.demo.pagos.model.EstadoPago;
import com.example.demo.pagos.model.Pago;
import com.example.demo.pagos.repository.PagoRepository;
import com.example.demo.tributario.model.Contribuyente;
import com.example.demo.tributario.model.Declaracion;
import com.example.demo.tributario.repository.ContribuyenteRepository;
import com.example.demo.tributario.repository.DeclaracionRepository;
// import com.example.demo.multas.service.MultaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PagoService {

    private final PagoRepository pagoRepository;
    private final ContribuyenteRepository contribuyenteRepository;
    private final DeclaracionRepository declaracionRepository;
    // private final MultaService multaService;

    /**
     * Crear un nuevo pago
     */
    public PagoResponse crearPago(PagoRequest request) {
        log.info("Creando nuevo pago");

        // Validar contribuyente (siempre requerido)
        if (request.getContribuyenteId() == null) {
            throw new PagoException("El contribuyente es obligatorio");
        }
        
        Contribuyente contribuyente = contribuyenteRepository.findById(request.getContribuyenteId())
                .orElseThrow(() -> new PagoException("Contribuyente no encontrado con ID: " + request.getContribuyenteId()));

        // Validar declaración si se proporciona
        Declaracion declaracion = null;
        if (request.getDeclaracionId() != null) {
            declaracion = declaracionRepository.findById(request.getDeclaracionId())
                    .orElseThrow(() -> new PagoException("Declaración no encontrada con ID: " + request.getDeclaracionId()));
        }

        // Validar referencia duplicada si se proporciona
        if (request.getReferencia() != null && !request.getReferencia().trim().isEmpty()) {
            if (pagoRepository.existsByReferenciaAndEstadoNot(request.getReferencia(), EstadoPago.ANULADO)) {
                throw new PagoException("Ya existe un pago con la referencia: " + request.getReferencia());
            }
        }

        // Crear el pago
        Pago pago = Pago.builder()
                .contribuyente(contribuyente)
                .declaracion(declaracion)
                .multaId(request.getMultaId())
                .monto(request.getMonto())
                .metodoPago(request.getMetodoPago())
                .referencia(request.getReferencia())
                .concepto(request.getConcepto())
                .fechaPago(request.getFechaPago() != null ? request.getFechaPago() : LocalDateTime.now())
                .estado(EstadoPago.PENDIENTE)
                .usuarioRegistro(request.getUsuarioRegistro())
                .build();

        Pago pagoGuardado = pagoRepository.save(pago);
        log.info("Pago creado exitosamente con ID: {}", pagoGuardado.getId());
        return convertirAResponse(pagoGuardado);
    }

    /**
     * Obtener pago por ID
     */
    @Transactional(readOnly = true)
    public Optional<PagoResponse> obtenerPagoPorId(Long id) {
        return pagoRepository.findById(id)
                .map(this::convertirAResponse);
    }

    /**
     * Listar todos los pagos con paginación
     */
    @Transactional(readOnly = true)
    public Page<PagoResponse> listarPagos(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaPago").descending());
        return pagoRepository.findAll(pageable)
                .map(this::convertirAResponse);
    }

    /**
     * Obtener pagos por contribuyente
     */
    @Transactional(readOnly = true)
    public List<PagoResponse> obtenerPagosPorContribuyente(Long contribuyenteId) {
        return pagoRepository.findByContribuyenteIdOrderByFechaPagoDesc(contribuyenteId)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener pagos por declaración
     */
    @Transactional(readOnly = true)
    public List<PagoResponse> obtenerPagosPorDeclaracion(Long declaracionId) {
        return pagoRepository.findByDeclaracionIdOrderByFechaPagoDesc(declaracionId)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener pagos por estado
     */
    @Transactional(readOnly = true)
    public List<PagoResponse> obtenerPagosPorEstado(EstadoPago estado) {
        return pagoRepository.findByEstadoOrderByFechaPagoDesc(estado)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Procesar pago (cambiar estado a PROCESADO)
     */
    public PagoResponse procesarPago(Long pagoId, String usuarioRegistro) {
        log.info("Procesando pago con ID: {}", pagoId);

        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new PagoException("Pago no encontrado con ID: " + pagoId));

        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new PagoException("Solo se pueden procesar pagos en estado PENDIENTE");
        }

        pago.setEstado(EstadoPago.PROCESADO);

        Pago pagoActualizado = pagoRepository.save(pago);
        log.info("Pago procesado exitosamente con ID: {}", pagoId);
        return convertirAResponse(pagoActualizado);
    }

    /**
     * Confirmar pago (cambiar estado a CONFIRMADO)
     */
    public PagoResponse confirmarPago(Long pagoId, String usuarioRegistro) {
        log.info("Confirmando pago con ID: {}", pagoId);

        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new PagoException("Pago no encontrado con ID: " + pagoId));

        if (pago.getEstado() != EstadoPago.PROCESADO) {
            throw new PagoException("Solo se pueden confirmar pagos en estado PROCESADO");
        }

        pago.setEstado(EstadoPago.CONFIRMADO);
        pago.setUsuarioRegistro(usuarioRegistro);
        
        Pago pagoActualizado = pagoRepository.save(pago);
        log.info("Pago confirmado exitosamente: {}", pagoId);

        return convertirAResponse(pagoActualizado);
    }

    /**
     * Anular pago
     */
    public PagoResponse anularPago(Long pagoId, String usuarioRegistro) {
        log.info("Anulando pago con ID: {}", pagoId);

        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new PagoException("Pago no encontrado con ID: " + pagoId));

        if (pago.getEstado() == EstadoPago.CONFIRMADO) {
            throw new PagoException("No se pueden anular pagos confirmados");
        }

        pago.setEstado(EstadoPago.ANULADO);
        pago.setUsuarioRegistro(usuarioRegistro);
        
        Pago pagoActualizado = pagoRepository.save(pago);
        log.info("Pago anulado exitosamente: {}", pagoId);

        return convertirAResponse(pagoActualizado);
    }

    /**
     * Obtener total pagado por contribuyente
     */
    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalPagadoPorContribuyente(Long contribuyenteId) {
        return pagoRepository.sumMontoByContribuyenteAndEstado(contribuyenteId, EstadoPago.CONFIRMADO);
    }

    /**
     * Obtener pagos recientes
     */
    @Transactional(readOnly = true)
    public List<PagoResponse> obtenerPagosRecientes(int limite) {
        Pageable pageable = PageRequest.of(0, limite);
        return pagoRepository.findRecentPayments(pageable)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Buscar pagos por concepto
     */
    @Transactional(readOnly = true)
    public List<PagoResponse> buscarPagosPorConcepto(String concepto) {
        return pagoRepository.findByConceptoContainingIgnoreCase(concepto)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener pagos del día
     */
    @Transactional(readOnly = true)
    public List<PagoResponse> obtenerPagosDelDia() {
        return pagoRepository.findPagosDelDia()
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convertir entidad Pago a PagoResponse
     */
    private PagoResponse convertirAResponse(Pago pago) {
        return PagoResponse.builder()
                .id(pago.getId())
                .contribuyenteId(pago.getContribuyente() != null ? pago.getContribuyente().getId() : null)
                .contribuyenteNombre(pago.getContribuyente() != null ? obtenerNombreContribuyente(pago.getContribuyente()) : null)
                .contribuyenteRif(pago.getContribuyente() != null ? pago.getContribuyente().getRif() : null)
                .declaracionId(pago.getDeclaracion() != null ? pago.getDeclaracion().getId() : null)
                .multaId(pago.getMultaId())
                .monto(pago.getMonto())
                .metodoPago(pago.getMetodoPago())
                .metodoPagoDescripcion(pago.getMetodoPago().getDescripcion())
                .referencia(pago.getReferencia())
                .estado(pago.getEstado())
                .estadoDescripcion(pago.getEstado().getDescripcion())
                .fechaPago(pago.getFechaPago())
                .concepto(pago.getConcepto())
                .comprobantePath(pago.getComprobantePath())
                .creadoEn(pago.getCreadoEn())
                .usuarioRegistro(pago.getUsuarioRegistro())
                .build();
    }

    /**
     * Obtener nombre del contribuyente (persona natural o jurídica)
     */
    private String obtenerNombreContribuyente(Contribuyente contribuyente) {
        if (contribuyente.getRazonSocial() != null && !contribuyente.getRazonSocial().trim().isEmpty()) {
            return contribuyente.getRazonSocial();
        } else {
            return (contribuyente.getNombre() != null ? contribuyente.getNombre() : "") + " " +
                   (contribuyente.getApellido() != null ? contribuyente.getApellido() : "");
        }
    }

    /**
     * Actualizar un pago existente
     */
    public PagoResponse actualizarPago(Long id, PagoRequest request) {
        log.info("Actualizando pago con ID: {}", id);

        // Buscar el pago existente
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new PagoException("Pago no encontrado con ID: " + id));

        // Validar que el pago se pueda editar (solo si no está confirmado)
        if (pago.getEstado() == EstadoPago.CONFIRMADO) {
            throw new PagoException("No se puede editar un pago confirmado");
        }

        // Validar contribuyente
        Contribuyente contribuyente = contribuyenteRepository.findById(request.getContribuyenteId())
                .orElseThrow(() -> new PagoException("Contribuyente no encontrado con ID: " + request.getContribuyenteId()));

        // Validar declaración si se proporciona
        Declaracion declaracion = null;
        if (request.getDeclaracionId() != null) {
            declaracion = declaracionRepository.findById(request.getDeclaracionId())
                    .orElseThrow(() -> new PagoException("Declaración no encontrada con ID: " + request.getDeclaracionId()));
        }

        // Actualizar campos del pago
        pago.setContribuyente(contribuyente);
        pago.setDeclaracion(declaracion);
        pago.setMonto(request.getMonto());
        pago.setMetodoPago(request.getMetodoPago());
        pago.setReferencia(request.getReferencia());
        pago.setConcepto(request.getConcepto());
        pago.setUsuarioRegistro(request.getUsuarioRegistro());

        // Guardar cambios
        Pago pagoActualizado = pagoRepository.save(pago);
        
        log.info("Pago actualizado exitosamente con ID: {}", pagoActualizado.getId());
        return convertirAResponse(pagoActualizado);
    }

    /**
     * Exportar pagos a Excel
     */
    public void exportarPagosExcel(HttpServletResponse response) throws IOException {
        try {
            List<Pago> pagos = pagoRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaPago"));
            
            // Crear workbook de Excel
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Pagos");
            
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
            String[] headers = {"ID", "Contribuyente", "RIF", "Concepto", "Monto", "Método Pago", "Estado", "Referencia", "Fecha Pago", "Usuario Registro"};
            
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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            for (Pago pago : pagos) {
                Row row = sheet.createRow(rowNum++);
                
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(pago.getId());
                cell0.setCellStyle(dataStyle);
                
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(obtenerNombreContribuyente(pago.getContribuyente()));
                cell1.setCellStyle(dataStyle);
                
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(pago.getContribuyente().getRif());
                cell2.setCellStyle(dataStyle);
                
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(pago.getConcepto());
                cell3.setCellStyle(dataStyle);
                
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(pago.getMonto().doubleValue());
                cell4.setCellStyle(dataStyle);
                
                Cell cell5 = row.createCell(5);
                cell5.setCellValue(pago.getMetodoPago().getDescripcion());
                cell5.setCellStyle(dataStyle);
                
                Cell cell6 = row.createCell(6);
                cell6.setCellValue(pago.getEstado().getDescripcion());
                cell6.setCellStyle(dataStyle);
                
                Cell cell7 = row.createCell(7);
                cell7.setCellValue(pago.getReferencia() != null ? pago.getReferencia() : "");
                cell7.setCellStyle(dataStyle);
                
                Cell cell8 = row.createCell(8);
                cell8.setCellValue(pago.getFechaPago().format(formatter));
                cell8.setCellStyle(dataStyle);
                
                Cell cell9 = row.createCell(9);
                cell9.setCellValue(pago.getUsuarioRegistro() != null ? pago.getUsuarioRegistro() : "");
                cell9.setCellStyle(dataStyle);
            }
            
            // Auto-ajustar el ancho de las columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Configurar respuesta HTTP
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"pagos.xlsx\"");
            
            // Escribir el archivo Excel a la respuesta
            workbook.write(response.getOutputStream());
            workbook.close();
            
        } catch (Exception e) {
            log.error("Error al generar archivo Excel de pagos", e);
            throw new IOException("Error al generar archivo Excel", e);
        }
    }
}
