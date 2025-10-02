package com.example.demo.servicios.service;

import com.example.demo.servicios.domain.EstadoServicio;
import com.example.demo.servicios.domain.Servicio;
import com.example.demo.servicios.domain.TipoServicio;
import com.example.demo.servicios.dto.ServicioRequest;
import com.example.demo.servicios.dto.ServicioResponse;
import com.example.demo.servicios.exception.ServicioException;
import com.example.demo.servicios.repository.ServicioRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de Servicios Municipales
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final ContribuyenteRepository contribuyenteRepository;

    /**
     * Obtener servicios con paginación y filtros
     */
    @Transactional(readOnly = true)
    public Page<ServicioResponse> obtenerServiciosPaginados(int page, int size, String numeroServicio, 
            String contribuyenteNombre, EstadoServicio estado, TipoServicio tipoServicio) {
        
        log.info("Obteniendo servicios paginados - Página: {}, Tamaño: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaRegistro").descending());
        
        Page<Servicio> serviciosPage = servicioRepository.findServiciosConFiltros(
                numeroServicio, contribuyenteNombre, estado, tipoServicio, pageable);
        
        return serviciosPage.map(this::convertirAResponse);
    }

    /**
     * Obtener servicio por ID
     */
    @Transactional(readOnly = true)
    public Optional<ServicioResponse> obtenerServicioPorId(Long id) {
        log.info("Obteniendo servicio por ID: {}", id);
        return servicioRepository.findById(id).map(this::convertirAResponse);
    }

    /**
     * Obtener servicio por número
     */
    @Transactional(readOnly = true)
    public Optional<ServicioResponse> obtenerServicioPorNumero(String numeroServicio) {
        log.info("Obteniendo servicio por número: {}", numeroServicio);
        return servicioRepository.findByNumeroServicio(numeroServicio).map(this::convertirAResponse);
    }

    /**
     * Crear nuevo servicio
     */
    public ServicioResponse crearServicio(ServicioRequest request) {
        log.info("Creando nuevo servicio para contribuyente ID: {}", request.getContribuyenteId());
        
        // Validar contribuyente
        Contribuyente contribuyente = contribuyenteRepository.findById(request.getContribuyenteId())
                .orElseThrow(() -> new ServicioException("Contribuyente no encontrado con ID: " + request.getContribuyenteId()));
        
        // Crear servicio
        Servicio servicio = new Servicio();
        servicio.setContribuyente(contribuyente);
        servicio.setTipoServicio(request.getTipoServicio());
        servicio.setEstado(request.getEstado() != null ? request.getEstado() : EstadoServicio.ACTIVO);
        servicio.setTarifaBase(request.getTarifaBase());
        servicio.setConsumoActual(request.getConsumoActual() != null ? request.getConsumoActual() : BigDecimal.ZERO);
        servicio.setConsumoAnterior(request.getConsumoAnterior() != null ? request.getConsumoAnterior() : BigDecimal.ZERO);
        servicio.setMontoFacturado(request.getMontoFacturado() != null ? request.getMontoFacturado() : BigDecimal.ZERO);
        servicio.setFechaInstalacion(request.getFechaInstalacion());
        servicio.setFechaUltimaLectura(request.getFechaUltimaLectura());
        servicio.setFechaProximaLectura(request.getFechaProximaLectura());
        servicio.setFechaCorte(request.getFechaCorte());
        servicio.setDireccionServicio(request.getDireccionServicio());
        servicio.setMedidor(request.getMedidor());
        servicio.setObservaciones(request.getObservaciones());
        servicio.setUsuarioRegistro(request.getUsuarioRegistro());
        
        // Generar número de servicio
        generarNumeroServicio(servicio);
        
        // Calcular facturación inicial si es necesario
        if (servicio.getMontoFacturado().equals(BigDecimal.ZERO) && 
            servicio.getTipoServicio().esTarifaFija()) {
            servicio.setMontoFacturado(servicio.getTarifaBase());
        }
        
        Servicio servicioGuardado = servicioRepository.save(servicio);
        log.info("Servicio creado exitosamente con ID: {} y número: {}", 
                servicioGuardado.getId(), servicioGuardado.getNumeroServicio());
        
        return convertirAResponse(servicioGuardado);
    }

    /**
     * Actualizar servicio existente
     */
    public ServicioResponse actualizarServicio(Long id, ServicioRequest request) {
        log.info("Actualizando servicio con ID: {}", id);
        
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ServicioException("Servicio no encontrado con ID: " + id));
        
        // Validar contribuyente si cambió
        if (!servicio.getContribuyente().getId().equals(request.getContribuyenteId())) {
            Contribuyente contribuyente = contribuyenteRepository.findById(request.getContribuyenteId())
                    .orElseThrow(() -> new ServicioException("Contribuyente no encontrado con ID: " + request.getContribuyenteId()));
            servicio.setContribuyente(contribuyente);
        }
        
        // Actualizar campos
        servicio.setTipoServicio(request.getTipoServicio());
        servicio.setEstado(request.getEstado());
        servicio.setTarifaBase(request.getTarifaBase());
        servicio.setConsumoActual(request.getConsumoActual());
        servicio.setConsumoAnterior(request.getConsumoAnterior());
        servicio.setMontoFacturado(request.getMontoFacturado());
        servicio.setFechaInstalacion(request.getFechaInstalacion());
        servicio.setFechaUltimaLectura(request.getFechaUltimaLectura());
        servicio.setFechaProximaLectura(request.getFechaProximaLectura());
        servicio.setFechaCorte(request.getFechaCorte());
        servicio.setDireccionServicio(request.getDireccionServicio());
        servicio.setMedidor(request.getMedidor());
        servicio.setObservaciones(request.getObservaciones());
        servicio.setUsuarioModificacion(request.getUsuarioRegistro());
        servicio.setFechaModificacion(LocalDateTime.now());
        
        Servicio servicioActualizado = servicioRepository.save(servicio);
        log.info("Servicio actualizado exitosamente con ID: {}", id);
        return convertirAResponse(servicioActualizado);
    }

    /**
     * Eliminar servicio
     */
    public void eliminarServicio(Long id) {
        log.info("Eliminando servicio con ID: {}", id);
        
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ServicioException("Servicio no encontrado con ID: " + id));
        
        // Verificar que el servicio no tenga deuda pendiente
        if (servicio.tieneDeudaPendiente()) {
            throw new ServicioException("No se puede eliminar un servicio con deuda pendiente");
        }
        
        servicioRepository.delete(servicio);
        log.info("Servicio eliminado exitosamente: {}", id);
    }

    /**
     * Registrar pago de servicio
     */
    public ServicioResponse registrarPago(Long servicioId, BigDecimal montoPago, String usuario) {
        log.info("Registrando pago de {} para servicio ID: {}", montoPago, servicioId);
        
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new ServicioException("Servicio no encontrado con ID: " + servicioId));
        
        // Validar que el servicio permita pagos
        if (!servicio.getEstado().permiteFacturacion()) {
            throw new ServicioException("El servicio no permite pagos en su estado actual");
        }
        
        // Validar monto
        if (montoPago.compareTo(BigDecimal.ZERO) <= 0 || montoPago.compareTo(servicio.getSaldoPendiente()) > 0) {
            throw new ServicioException("Monto de pago inválido");
        }
        
        // Actualizar montos
        BigDecimal nuevoMontoPagado = servicio.getMontoPagado().add(montoPago);
        servicio.setMontoPagado(nuevoMontoPagado);
        
        // Si está completamente pagado y estaba suspendido por deuda, reactivar
        if (nuevoMontoPagado.compareTo(servicio.getMontoFacturado()) >= 0 && 
            servicio.getEstado() == EstadoServicio.SUSPENDIDO) {
            servicio.setEstado(EstadoServicio.ACTIVO);
        }
        
        servicio.setUsuarioModificacion(usuario);
        servicio.setFechaModificacion(LocalDateTime.now());
        
        servicioRepository.save(servicio);
        log.info("Pago registrado exitosamente para servicio: {}", servicioId);
        
        return convertirAResponse(servicio);
    }

    /**
     * Cambiar estado de servicio
     */
    public ServicioResponse cambiarEstado(Long servicioId, EstadoServicio nuevoEstado, String usuario) {
        log.info("Cambiando estado de servicio {} a {}", servicioId, nuevoEstado);
        
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new ServicioException("Servicio no encontrado con ID: " + servicioId));
        
        // Validar transición de estado
        validarCambioEstado(servicio.getEstado(), nuevoEstado, servicio);
        
        servicio.setEstado(nuevoEstado);
        servicio.setUsuarioModificacion(usuario);
        servicio.setFechaModificacion(LocalDateTime.now());
        
        servicioRepository.save(servicio);
        log.info("Estado cambiado exitosamente para servicio: {}", servicioId);
        
        return convertirAResponse(servicio);
    }

    /**
     * Obtener estadísticas de servicios
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        log.info("Obteniendo estadísticas de servicios");
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Estadísticas por estado
        List<Object[]> estadoStats = servicioRepository.countByEstado();
        Map<String, Long> estadoMap = estadoStats.stream()
                .collect(Collectors.toMap(
                        arr -> ((EstadoServicio) arr[0]).name(),
                        arr -> (Long) arr[1]
                ));
        estadisticas.put("porEstado", estadoMap);
        
        // Estadísticas por tipo
        List<Object[]> tipoStats = servicioRepository.countByTipoServicio();
        Map<String, Long> tipoMap = tipoStats.stream()
                .collect(Collectors.toMap(
                        arr -> ((TipoServicio) arr[0]).name(),
                        arr -> (Long) arr[1]
                ));
        estadisticas.put("porTipo", tipoMap);
        
        // Estadísticas financieras
        estadisticas.put("totalFacturado", servicioRepository.sumTotalFacturado());
        estadisticas.put("totalPagado", servicioRepository.sumTotalPagado());
        estadisticas.put("saldoPendiente", servicioRepository.sumSaldoPendiente());
        estadisticas.put("totalServicios", servicioRepository.count());
        
        return estadisticas;
    }

    /**
     * Generar número único de servicio
     */
    private void generarNumeroServicio(Servicio servicio) {
        String year = String.valueOf(LocalDate.now().getYear());
        Long sequence = servicioRepository.getNextSequenceForYear(year);
        String numeroServicio = String.format("SRV-%s-%04d", year, sequence);
        servicio.setNumeroServicio(numeroServicio);
    }

    /**
     * Validar cambio de estado
     */
    private void validarCambioEstado(EstadoServicio estadoActual, EstadoServicio nuevoEstado, Servicio servicio) {
        switch (nuevoEstado) {
            case CORTADO:
                if (!estadoActual.puedeSerCortado()) {
                    throw new ServicioException("El servicio no puede ser cortado desde su estado actual");
                }
                break;
            case ACTIVO:
                if (estadoActual == EstadoServicio.CORTADO && servicio.tieneDeudaPendiente()) {
                    throw new ServicioException("No se puede reactivar un servicio con deuda pendiente");
                }
                break;
            case INACTIVO:
                if (servicio.tieneDeudaPendiente()) {
                    throw new ServicioException("No se puede inactivar un servicio con deuda pendiente");
                }
                break;
            case SUSPENDIDO:
                // Validaciones para suspender servicio
                break;
            case MANTENIMIENTO:
                // Validaciones para poner en mantenimiento
                break;
            case PENDIENTE_INSTALACION:
                // Validaciones para estado pendiente de instalación
                break;
        }
    }

    /**
     * Convertir entidad a DTO de respuesta
     */
    private ServicioResponse convertirAResponse(Servicio servicio) {
        ServicioResponse response = new ServicioResponse();
        response.setId(servicio.getId());
        response.setNumeroServicio(servicio.getNumeroServicio());
        response.setContribuyenteId(servicio.getContribuyente().getId());
        response.setContribuyenteNombre(obtenerNombreContribuyente(servicio.getContribuyente()));
        response.setContribuyenteRif(servicio.getContribuyente().getRif());
        response.setTipoServicio(servicio.getTipoServicio());
        response.setTipoServicioDescripcion(servicio.getTipoServicio().getDescripcion());
        response.setEstado(servicio.getEstado());
        response.setEstadoDescripcion(servicio.getEstado().getDescripcion());
        response.setEstadoColorClass(servicio.getEstado().getColorClass());
        response.setTarifaBase(servicio.getTarifaBase());
        response.setConsumoActual(servicio.getConsumoActual());
        response.setConsumoAnterior(servicio.getConsumoAnterior());
        response.setConsumoDelPeriodo(servicio.getConsumoDelPeriodo());
        response.setMontoFacturado(servicio.getMontoFacturado());
        response.setMontoPagado(servicio.getMontoPagado());
        response.setSaldoPendiente(servicio.getSaldoPendiente());
        response.setFechaInstalacion(servicio.getFechaInstalacion());
        response.setFechaUltimaLectura(servicio.getFechaUltimaLectura());
        response.setFechaProximaLectura(servicio.getFechaProximaLectura());
        response.setFechaCorte(servicio.getFechaCorte());
        response.setDireccionServicio(servicio.getDireccionServicio());
        response.setMedidor(servicio.getMedidor());
        response.setObservaciones(servicio.getObservaciones());
        response.setFechaRegistro(servicio.getFechaRegistro());
        response.setFechaModificacion(servicio.getFechaModificacion());
        response.setUsuarioRegistro(servicio.getUsuarioRegistro());
        response.setUsuarioModificacion(servicio.getUsuarioModificacion());
        
        // Campos calculados
        response.setTieneDeudaPendiente(servicio.tieneDeudaPendiente());
        response.setEstaVencidoParaCorte(servicio.estaVencidoParaCorte());
        response.setRequiereMedidor(servicio.getTipoServicio().requiereMedidor());
        response.setEsFacturablePorConsumo(servicio.getTipoServicio().esFacturablePorConsumo());
        response.setEsOperativo(servicio.getEstado().esOperativo());
        response.setPermiteFacturacion(servicio.getEstado().permiteFacturacion());
        
        return response;
    }

    /**
     * Obtener nombre del contribuyente
     */
    private String obtenerNombreContribuyente(Contribuyente contribuyente) {
        if (contribuyente.getRazonSocial() != null && !contribuyente.getRazonSocial().trim().isEmpty()) {
            return contribuyente.getRazonSocial();
        } else {
            return (contribuyente.getNombre() != null ? contribuyente.getNombre() : "") + " " +
                   (contribuyente.getApellido() != null ? contribuyente.getApellido() : "");
        }
    }
}
