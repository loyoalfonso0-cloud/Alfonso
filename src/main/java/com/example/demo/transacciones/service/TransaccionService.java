package com.example.demo.transacciones.service;

import com.example.demo.transacciones.dto.TransaccionRequest;
import com.example.demo.transacciones.dto.TransaccionResponse;
import com.example.demo.transacciones.model.EstadoTransaccion;
import com.example.demo.transacciones.model.TipoTransaccion;
import com.example.demo.transacciones.model.Transaccion;
import com.example.demo.transacciones.repository.TransaccionRepository;
import com.example.demo.tributario.repository.ContribuyenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransaccionService {

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private ContribuyenteRepository contribuyenteRepository;

    // Crear nueva transacción
    public TransaccionResponse crearTransaccion(TransaccionRequest request) {
        Transaccion transaccion = new Transaccion();
        mapearRequestAEntidad(request, transaccion);
        
        // Generar número de transacción único
        transaccion.setNumeroTransaccion(generarNumeroTransaccion());
        
        // Establecer usuario actual
        transaccion.setUsuarioRegistro(obtenerUsuarioActual());
        
        // Si no se especifica fecha, usar la actual
        if (transaccion.getFechaTransaccion() == null) {
            transaccion.setFechaTransaccion(LocalDateTime.now());
        }
        
        transaccion = transaccionRepository.save(transaccion);
        return mapearEntidadAResponse(transaccion);
    }

    // Obtener transacción por ID
    @Transactional(readOnly = true)
    public Optional<TransaccionResponse> obtenerTransaccionPorId(Long id) {
        return transaccionRepository.findById(id)
                .map(this::mapearEntidadAResponse);
    }

    // Obtener transacción por número
    @Transactional(readOnly = true)
    public Optional<TransaccionResponse> obtenerTransaccionPorNumero(String numeroTransaccion) {
        return transaccionRepository.findByNumeroTransaccion(numeroTransaccion)
                .map(this::mapearEntidadAResponse);
    }

    // Listar todas las transacciones con paginación
    @Transactional(readOnly = true)
    public Page<TransaccionResponse> listarTransacciones(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return transaccionRepository.findAll(pageable)
                .map(this::mapearEntidadAResponse);
    }

    // Actualizar transacción
    public TransaccionResponse actualizarTransaccion(Long id, TransaccionRequest request) {
        Transaccion transaccion = transaccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        // Solo permitir actualización si está pendiente
        if (!transaccion.isPendiente()) {
            throw new RuntimeException("Solo se pueden actualizar transacciones pendientes");
        }

        mapearRequestAEntidad(request, transaccion);
        transaccion.setFechaModificacion(LocalDateTime.now());
        
        transaccion = transaccionRepository.save(transaccion);
        return mapearEntidadAResponse(transaccion);
    }

    // Procesar transacción
    public TransaccionResponse procesarTransaccion(Long id) {
        Transaccion transaccion = transaccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        if (!transaccion.isPendiente()) {
            throw new RuntimeException("Solo se pueden procesar transacciones pendientes");
        }

        transaccion.procesar(obtenerUsuarioActual());
        transaccion = transaccionRepository.save(transaccion);
        
        return mapearEntidadAResponse(transaccion);
    }

    // Confirmar transacción
    public TransaccionResponse confirmarTransaccion(Long id) {
        Transaccion transaccion = transaccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        if (!EstadoTransaccion.PROCESADA.equals(transaccion.getEstado())) {
            throw new RuntimeException("Solo se pueden confirmar transacciones procesadas");
        }

        transaccion.confirmar();
        transaccion = transaccionRepository.save(transaccion);
        
        return mapearEntidadAResponse(transaccion);
    }

    // Anular transacción
    public TransaccionResponse anularTransaccion(Long id, String motivo) {
        Transaccion transaccion = transaccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        if (transaccion.isAnulada()) {
            throw new RuntimeException("La transacción ya está anulada");
        }

        transaccion.anular(motivo);
        transaccion = transaccionRepository.save(transaccion);
        
        return mapearEntidadAResponse(transaccion);
    }

    // Eliminar transacción (solo si está pendiente)
    public void eliminarTransaccion(Long id) {
        Transaccion transaccion = transaccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        if (!transaccion.isPendiente()) {
            throw new RuntimeException("Solo se pueden eliminar transacciones pendientes");
        }

        transaccionRepository.delete(transaccion);
    }

    // Búsqueda avanzada
    @Transactional(readOnly = true)
    public Page<TransaccionResponse> busquedaAvanzada(
            String numeroTransaccion, Long contribuyenteId, EstadoTransaccion estado,
            TipoTransaccion tipoTransaccion, LocalDateTime fechaInicio, LocalDateTime fechaFin,
            BigDecimal montoMinimo, BigDecimal montoMaximo, int page, int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaTransaccion").descending());
        
        return transaccionRepository.busquedaAvanzada(
                numeroTransaccion, contribuyenteId, estado, tipoTransaccion,
                fechaInicio, fechaFin, montoMinimo, montoMaximo, pageable
        ).map(this::mapearEntidadAResponse);
    }

    // Obtener estadísticas del dashboard
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Contadores básicos
        estadisticas.put("totalTransacciones", transaccionRepository.countTotalTransacciones());
        estadisticas.put("transaccionesPendientes", transaccionRepository.countByEstado(EstadoTransaccion.PENDIENTE));
        estadisticas.put("transaccionesProcesadas", transaccionRepository.countByEstado(EstadoTransaccion.PROCESADA));
        estadisticas.put("transaccionesConfirmadas", transaccionRepository.countByEstado(EstadoTransaccion.CONFIRMADA));
        
        // Montos
        estadisticas.put("ingresosHoy", transaccionRepository.sumIngresosHoy());
        estadisticas.put("egresosHoy", transaccionRepository.sumEgresosHoy());
        estadisticas.put("balanceMes", transaccionRepository.balanceMesActual());
        
        // Montos por estado
        estadisticas.put("montoPendiente", transaccionRepository.sumMontoByEstado(EstadoTransaccion.PENDIENTE));
        estadisticas.put("montoProcesado", transaccionRepository.sumMontoByEstado(EstadoTransaccion.PROCESADA));
        estadisticas.put("montoConfirmado", transaccionRepository.sumMontoByEstado(EstadoTransaccion.CONFIRMADA));
        
        return estadisticas;
    }

    // Obtener transacciones recientes
    @Transactional(readOnly = true)
    public List<TransaccionResponse> obtenerTransaccionesRecientes(int limite) {
        Pageable pageable = PageRequest.of(0, limite);
        return transaccionRepository.findTransaccionesRecientes(pageable)
                .stream()
                .map(this::mapearEntidadAResponse)
                .collect(Collectors.toList());
    }

    // Obtener transacciones por contribuyente
    @Transactional(readOnly = true)
    public Page<TransaccionResponse> obtenerTransaccionesPorContribuyente(Long contribuyenteId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaTransaccion").descending());
        return transaccionRepository.findByContribuyenteId(contribuyenteId, pageable)
                .map(this::mapearEntidadAResponse);
    }

    // Generar reporte por período
    @Transactional(readOnly = true)
    public List<Map<String, Object>> generarReportePorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<Object[]> resultados = transaccionRepository.reporteTransaccionesPorPeriodo(fechaInicio, fechaFin);
        
        return resultados.stream().map(resultado -> {
            Map<String, Object> item = new HashMap<>();
            item.put("tipoTransaccion", resultado[0]);
            item.put("cantidad", resultado[1]);
            item.put("montoTotal", resultado[2]);
            return item;
        }).collect(Collectors.toList());
    }

    // Métodos privados de utilidad
    private void mapearRequestAEntidad(TransaccionRequest request, Transaccion transaccion) {
        transaccion.setTipoTransaccion(request.getTipoTransaccion());
        transaccion.setMonto(request.getMonto());
        transaccion.setConcepto(request.getConcepto());
        transaccion.setReferenciaExterna(request.getReferenciaExterna());
        transaccion.setNumeroComprobante(request.getNumeroComprobante());
        transaccion.setEntidadRelacionadaTipo(request.getEntidadRelacionadaTipo());
        transaccion.setEntidadRelacionadaId(request.getEntidadRelacionadaId());
        transaccion.setObservaciones(request.getObservaciones());
        
        if (request.getFechaTransaccion() != null) {
            transaccion.setFechaTransaccion(request.getFechaTransaccion());
        }
        
        // Establecer contribuyente si se proporciona
        if (request.getContribuyenteId() != null) {
            contribuyenteRepository.findById(request.getContribuyenteId())
                    .ifPresent(transaccion::setContribuyente);
        }
    }

    private TransaccionResponse mapearEntidadAResponse(Transaccion transaccion) {
        TransaccionResponse response = new TransaccionResponse();
        
        response.setId(transaccion.getId());
        response.setNumeroTransaccion(transaccion.getNumeroTransaccion());
        response.setTipoTransaccion(transaccion.getTipoTransaccion());
        response.setEstado(transaccion.getEstado());
        response.setMonto(transaccion.getMonto());
        response.setConcepto(transaccion.getConcepto());
        response.setReferenciaExterna(transaccion.getReferenciaExterna());
        response.setNumeroComprobante(transaccion.getNumeroComprobante());
        response.setEntidadRelacionadaTipo(transaccion.getEntidadRelacionadaTipo());
        response.setEntidadRelacionadaId(transaccion.getEntidadRelacionadaId());
        response.setFechaTransaccion(transaccion.getFechaTransaccion());
        response.setFechaProcesamiento(transaccion.getFechaProcesamiento());
        response.setFechaConfirmacion(transaccion.getFechaConfirmacion());
        response.setObservaciones(transaccion.getObservaciones());
        response.setUsuarioRegistro(transaccion.getUsuarioRegistro());
        response.setUsuarioProcesamiento(transaccion.getUsuarioProcesamiento());
        response.setFechaRegistro(transaccion.getFechaRegistro());
        response.setFechaModificacion(transaccion.getFechaModificacion());
        
        // Mapear datos del contribuyente
        if (transaccion.getContribuyente() != null) {
            response.setContribuyenteId(transaccion.getContribuyente().getId());
            response.setContribuyenteNombre(transaccion.getContribuyente().getRazonSocial());
            response.setContribuyenteRif(transaccion.getContribuyente().getRif());
        }
        
        return response;
    }

    private String generarNumeroTransaccion() {
        String prefijo = "TXN-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy"));
        String timestamp = String.valueOf(System.currentTimeMillis());
        String numeroBase = prefijo + "-" + timestamp.substring(timestamp.length() - 8);
        
        // Verificar que no exista y generar uno nuevo si es necesario
        int contador = 1;
        String numeroFinal = numeroBase;
        while (transaccionRepository.existsByNumeroTransaccion(numeroFinal)) {
            numeroFinal = numeroBase + "-" + String.format("%03d", contador);
            contador++;
        }
        
        return numeroFinal;
    }

    private String obtenerUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "SYSTEM";
    }
}
