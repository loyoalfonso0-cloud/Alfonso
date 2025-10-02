package com.example.demo.multas.service;

import com.example.demo.multas.dto.MultaRequest;
import com.example.demo.multas.dto.MultaResponse;
import com.example.demo.multas.exception.MultaException;
import com.example.demo.multas.model.EstadoMulta;
import com.example.demo.multas.model.Multa;
import com.example.demo.multas.repository.MultaRepository;
import com.example.demo.tributario.model.Contribuyente;
import com.example.demo.tributario.repository.ContribuyenteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MultaService {
    
    private final MultaRepository multaRepository;
    private final ContribuyenteRepository contribuyenteRepository;
    
    /**
     * Crear una nueva multa
     */
    public MultaResponse crearMulta(MultaRequest request) {
        log.info("Creando nueva multa para contribuyente: {}", request.getContribuyenteId());
        
        // Validar contribuyente
        Contribuyente contribuyente = contribuyenteRepository.findById(request.getContribuyenteId())
                .orElseThrow(() -> new MultaException("Contribuyente no encontrado con ID: " + request.getContribuyenteId()));
        
        // Validar fechas
        if (!request.esFechaVencimientoValida()) {
            throw new MultaException("La fecha de vencimiento debe ser posterior a la fecha de infracción");
        }
        
        // Generar número de multa único
        String numeroMulta = generarNumeroMulta();
        
        // Crear entidad
        Multa multa = Multa.builder()
                .contribuyente(contribuyente)
                .numeroMulta(numeroMulta)
                .tipoInfraccion(request.getTipoInfraccion())
                .descripcion(request.getDescripcion())
                .monto(request.getMonto())
                .fechaInfraccion(request.getFechaInfraccion())
                .fechaVencimiento(request.getFechaVencimiento())
                .observaciones(request.getObservaciones())
                .usuarioRegistro(request.getUsuarioRegistro())
                .estado(EstadoMulta.PENDIENTE)
                .build();
        
        // Guardar
        Multa multaGuardada = multaRepository.save(multa);
        
        log.info("Multa creada exitosamente con ID: {} y número: {}", multaGuardada.getId(), numeroMulta);
        return convertirAResponse(multaGuardada);
    }
    
    /**
     * Obtener multa por ID
     */
    @Transactional(readOnly = true)
    public MultaResponse obtenerMultaPorId(Long id) {
        Multa multa = multaRepository.findById(id)
                .orElseThrow(() -> new MultaException("Multa no encontrada con ID: " + id));
        return convertirAResponse(multa);
    }
    
    /**
     * Listar multas con paginación
     */
    @Transactional(readOnly = true)
    public Page<MultaResponse> listarMultas(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return multaRepository.findAll(pageable)
                .map(this::convertirAResponse);
    }
    
    /**
     * Actualizar multa
     */
    public MultaResponse actualizarMulta(Long id, MultaRequest request) {
        log.info("Actualizando multa con ID: {}", id);
        
        Multa multa = multaRepository.findById(id)
                .orElseThrow(() -> new MultaException("Multa no encontrada con ID: " + id));
        
        // Validar que se puede modificar
        if (!multa.getEstado().permiteModificacion()) {
            throw new MultaException("No se puede modificar una multa en estado: " + multa.getEstado().getDescripcion());
        }
        
        // Validar contribuyente si cambió
        if (!multa.getContribuyente().getId().equals(request.getContribuyenteId())) {
            Contribuyente contribuyente = contribuyenteRepository.findById(request.getContribuyenteId())
                    .orElseThrow(() -> new MultaException("Contribuyente no encontrado con ID: " + request.getContribuyenteId()));
            multa.setContribuyente(contribuyente);
        }
        
        // Actualizar campos
        multa.setTipoInfraccion(request.getTipoInfraccion());
        multa.setDescripcion(request.getDescripcion());
        multa.setMonto(request.getMonto());
        multa.setFechaInfraccion(request.getFechaInfraccion());
        multa.setFechaVencimiento(request.getFechaVencimiento());
        multa.setObservaciones(request.getObservaciones());
        multa.setUsuarioModificacion(request.getUsuarioRegistro());
        
        Multa multaActualizada = multaRepository.save(multa);
        
        log.info("Multa actualizada exitosamente con ID: {}", id);
        return convertirAResponse(multaActualizada);
    }
    
    /**
     * Anular multa
     */
    public MultaResponse anularMulta(Long id, String motivo, String usuario) {
        log.info("Anulando multa con ID: {}", id);
        
        Multa multa = multaRepository.findById(id)
                .orElseThrow(() -> new MultaException("Multa no encontrada con ID: " + id));
        
        if (multa.getEstado() == EstadoMulta.PAGADA) {
            throw new MultaException("No se puede anular una multa que ya está pagada");
        }
        
        multa.setEstado(EstadoMulta.ANULADA);
        multa.setObservaciones((multa.getObservaciones() != null ? multa.getObservaciones() + "\n" : "") + 
                              "ANULADA: " + motivo + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        multa.setUsuarioModificacion(usuario);
        
        Multa multaAnulada = multaRepository.save(multa);
        
        log.info("Multa anulada exitosamente con ID: {}", id);
        return convertirAResponse(multaAnulada);
    }
    
    /**
     * Registrar pago de multa
     */
    public MultaResponse registrarPago(Long id, BigDecimal montoPago, String usuario) {
        log.info("Registrando pago de {} para multa ID: {}", montoPago, id);
        
        Multa multa = multaRepository.findById(id)
                .orElseThrow(() -> new MultaException("Multa no encontrada con ID: " + id));
        
        if (!multa.getEstado().permitePago()) {
            throw new MultaException("No se puede registrar pago para multa en estado: " + multa.getEstado().getDescripcion());
        }
        
        BigDecimal nuevoMontoPagado = multa.getMontoPagado().add(montoPago);
        
        if (nuevoMontoPagado.compareTo(multa.getMonto()) > 0) {
            throw new MultaException("El monto a pagar excede el saldo pendiente de la multa");
        }
        
        multa.setMontoPagado(nuevoMontoPagado);
        multa.setUsuarioModificacion(usuario);
        
        // Actualizar estado según el pago
        if (nuevoMontoPagado.compareTo(multa.getMonto()) == 0) {
            multa.setEstado(EstadoMulta.PAGADA);
            multa.setFechaPago(LocalDateTime.now());
        } else {
            multa.setEstado(EstadoMulta.PARCIALMENTE_PAGADA);
        }
        
        Multa multaActualizada = multaRepository.save(multa);
        
        log.info("Pago registrado exitosamente para multa ID: {}", id);
        return convertirAResponse(multaActualizada);
    }
    
    /**
     * Buscar multas por contribuyente
     */
    @Transactional(readOnly = true)
    public Page<MultaResponse> buscarPorContribuyente(Long contribuyenteId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaRegistro"));
        return multaRepository.findByContribuyenteId(contribuyenteId, pageable)
                .map(this::convertirAResponse);
    }
    
    /**
     * Buscar multas por estado
     */
    @Transactional(readOnly = true)
    public Page<MultaResponse> buscarPorEstado(EstadoMulta estado, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaRegistro"));
        return multaRepository.findByEstado(estado, pageable)
                .map(this::convertirAResponse);
    }
    
    /**
     * Obtener estadísticas de multas
     */
    @Transactional(readOnly = true)
    public MultaEstadisticas obtenerEstadisticas() {
        Long totalMultas = multaRepository.count();
        Long multasPendientes = multaRepository.countByEstado(EstadoMulta.PENDIENTE);
        Long multasPagadas = multaRepository.countByEstado(EstadoMulta.PAGADA);
        Long multasVencidas = multaRepository.countByEstado(EstadoMulta.VENCIDA);
        
        BigDecimal montoTotal = multaRepository.sumMontoByEstado(EstadoMulta.PENDIENTE);
        BigDecimal montoPagado = multaRepository.sumMontoPagado();
        
        return MultaEstadisticas.builder()
                .totalMultas(totalMultas != null ? totalMultas : 0L)
                .multasPendientes(multasPendientes != null ? multasPendientes : 0L)
                .multasPagadas(multasPagadas != null ? multasPagadas : 0L)
                .multasVencidas(multasVencidas != null ? multasVencidas : 0L)
                .montoTotal(montoTotal != null ? montoTotal : BigDecimal.ZERO)
                .montoPagado(montoPagado != null ? montoPagado : BigDecimal.ZERO)
                .build();
    }
    
    /**
     * Actualizar multas vencidas
     */
    @Transactional
    public void actualizarMultasVencidas() {
        List<EstadoMulta> estadosPermitidos = List.of(EstadoMulta.PENDIENTE, EstadoMulta.PARCIALMENTE_PAGADA);
        List<Multa> multasVencidas = multaRepository.findMultasVencidas(LocalDateTime.now(), estadosPermitidos);
        
        multasVencidas.forEach(multa -> {
            multa.setEstado(EstadoMulta.VENCIDA);
            multa.setUsuarioModificacion("SISTEMA");
        });
        
        if (!multasVencidas.isEmpty()) {
            multaRepository.saveAll(multasVencidas);
            log.info("Actualizadas {} multas a estado VENCIDA", multasVencidas.size());
        }
    }
    
    /**
     * Generar número de multa único
     */
    private String generarNumeroMulta() {
        String prefijo = "MUL";
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        
        String numeroBase;
        int contador = 1;
        
        do {
            numeroBase = String.format("%s-%s-%04d", prefijo, fecha, contador);
            contador++;
        } while (multaRepository.existsByNumeroMulta(numeroBase));
        
        return numeroBase;
    }
    
    /**
     * Convertir entidad a DTO
     */
    private MultaResponse convertirAResponse(Multa multa) {
        String contribuyenteNombre = obtenerNombreContribuyente(multa.getContribuyente());
        
        int diasVencimiento = 0;
        if (multa.getFechaVencimiento() != null) {
            diasVencimiento = (int) ChronoUnit.DAYS.between(LocalDateTime.now(), multa.getFechaVencimiento());
        }
        
        return MultaResponse.builder()
                .id(multa.getId())
                .numeroMulta(multa.getNumeroMulta())
                .contribuyenteId(multa.getContribuyente().getId())
                .contribuyenteNombre(contribuyenteNombre)
                .contribuyenteRif(multa.getContribuyente().getRif())
                .tipoInfraccion(multa.getTipoInfraccion())
                .tipoInfraccionDescripcion(multa.getTipoInfraccion().getDescripcion())
                .descripcion(multa.getDescripcion())
                .monto(multa.getMonto())
                .montoPagado(multa.getMontoPagado())
                .saldoPendiente(multa.getSaldoPendiente())
                .estado(multa.getEstado())
                .estadoDescripcion(multa.getEstado().getDescripcion())
                .fechaInfraccion(multa.getFechaInfraccion())
                .fechaVencimiento(multa.getFechaVencimiento())
                .fechaPago(multa.getFechaPago())
                .observaciones(multa.getObservaciones())
                .estaPagada(multa.estaPagada())
                .estaVencida(multa.estaVencida())
                .diasVencimiento(diasVencimiento)
                .usuarioRegistro(multa.getUsuarioRegistro())
                .fechaRegistro(multa.getFechaRegistro())
                .usuarioModificacion(multa.getUsuarioModificacion())
                .fechaModificacion(multa.getFechaModificacion())
                .build();
    }
    
    /**
     * Obtener nombre completo del contribuyente
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
     * Clase interna para estadísticas
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MultaEstadisticas {
        private Long totalMultas;
        private Long multasPendientes;
        private Long multasPagadas;
        private Long multasVencidas;
        private BigDecimal montoTotal;
        private BigDecimal montoPagado;
        
        public BigDecimal getSaldoPendiente() {
            return montoTotal.subtract(montoPagado);
        }
    }
}
