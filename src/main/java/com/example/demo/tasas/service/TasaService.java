package com.example.demo.tasas.service;

import com.example.demo.tasas.dto.TasaRequest;
import com.example.demo.tasas.dto.TasaResponse;
import com.example.demo.tasas.exception.TasaException;
import com.example.demo.tasas.model.EstadoTasa;
import com.example.demo.tasas.model.Tasa;
import com.example.demo.tasas.model.TipoTasa;
import com.example.demo.tasas.repository.TasaRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de tasas municipales
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TasaService {

    private final TasaRepository tasaRepository;
    private final ContribuyenteRepository contribuyenteRepository;

    /**
     * Crear una nueva tasa
     */
    public TasaResponse crearTasa(TasaRequest request) {
        log.info("Creando nueva tasa para contribuyente: {}", request.getContribuyenteId());

        // Validar contribuyente
        Contribuyente contribuyente = contribuyenteRepository.findById(request.getContribuyenteId())
                .orElseThrow(() -> new TasaException("Contribuyente no encontrado con ID: " + request.getContribuyenteId()));

        // Generar número de tasa único
        String numeroTasa = generarNumeroTasa(request.getTipoTasa());

        // Crear la tasa
        Tasa tasa = Tasa.builder()
                .contribuyente(contribuyente)
                .numeroTasa(numeroTasa)
                .tipoTasa(request.getTipoTasa())
                .descripcion(request.getDescripcion())
                .montoBase(request.getMontoBase())
                .periodoFacturacion(request.getPeriodoFacturacion())
                .fechaInicio(request.getFechaInicio())
                .fechaVencimiento(request.getFechaVencimiento())
                .direccion(request.getDireccion())
                .zonaMunicipal(request.getZonaMunicipal())
                .areaInmueble(request.getAreaInmueble())
                .valorCatastral(request.getValorCatastral())
                .observaciones(request.getObservaciones())
                .usuarioRegistro(request.getUsuarioRegistro())
                .build();

        Tasa tasaGuardada = tasaRepository.save(tasa);
        log.info("Tasa creada exitosamente con ID: {}", tasaGuardada.getId());
        return convertirAResponse(tasaGuardada);
    }

    /**
     * Obtener tasa por ID
     */
    @Transactional(readOnly = true)
    public Optional<TasaResponse> obtenerTasaPorId(Long id) {
        return tasaRepository.findById(id)
                .map(this::convertirAResponse);
    }

    /**
     * Listar todas las tasas con paginación
     */
    @Transactional(readOnly = true)
    public Page<TasaResponse> listarTasas(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaRegistro").descending());
        return tasaRepository.findAll(pageable)
                .map(this::convertirAResponse);
    }

    /**
     * Obtener tasas por contribuyente
     */
    @Transactional(readOnly = true)
    public List<TasaResponse> obtenerTasasPorContribuyente(Long contribuyenteId) {
        return tasaRepository.findByContribuyenteId(contribuyenteId)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener tasas por estado
     */
    @Transactional(readOnly = true)
    public List<TasaResponse> obtenerTasasPorEstado(EstadoTasa estado) {
        return tasaRepository.findByEstado(estado)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener tasas por tipo
     */
    @Transactional(readOnly = true)
    public List<TasaResponse> obtenerTasasPorTipo(TipoTasa tipoTasa) {
        return tasaRepository.findByTipoTasa(tipoTasa)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Buscar tasas por múltiples criterios
     */
    @Transactional(readOnly = true)
    public Page<TasaResponse> buscarTasas(Long contribuyenteId, TipoTasa tipoTasa, 
                                          EstadoTasa estado, String zonaMunicipal, 
                                          int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaRegistro").descending());
        return tasaRepository.findByMultiplesCriterios(contribuyenteId, tipoTasa, estado, zonaMunicipal, pageable)
                .map(this::convertirAResponse);
    }

    /**
     * Actualizar una tasa
     */
    public TasaResponse actualizarTasa(Long id, TasaRequest request) {
        log.info("Actualizando tasa con ID: {}", id);

        Tasa tasa = tasaRepository.findById(id)
                .orElseThrow(() -> new TasaException("Tasa no encontrada con ID: " + id));

        // Validar contribuyente si cambió
        if (!tasa.getContribuyente().getId().equals(request.getContribuyenteId())) {
            Contribuyente contribuyente = contribuyenteRepository.findById(request.getContribuyenteId())
                    .orElseThrow(() -> new TasaException("Contribuyente no encontrado con ID: " + request.getContribuyenteId()));
            tasa.setContribuyente(contribuyente);
        }

        // Actualizar campos
        tasa.setTipoTasa(request.getTipoTasa());
        tasa.setDescripcion(request.getDescripcion());
        tasa.setMontoBase(request.getMontoBase());
        tasa.setPeriodoFacturacion(request.getPeriodoFacturacion());
        tasa.setFechaInicio(request.getFechaInicio());
        tasa.setFechaVencimiento(request.getFechaVencimiento());
        tasa.setDireccion(request.getDireccion());
        tasa.setZonaMunicipal(request.getZonaMunicipal());
        tasa.setAreaInmueble(request.getAreaInmueble());
        tasa.setValorCatastral(request.getValorCatastral());
        tasa.setObservaciones(request.getObservaciones());
        tasa.setUsuarioModificacion(request.getUsuarioRegistro());
        tasa.setFechaModificacion(LocalDateTime.now());

        Tasa tasaActualizada = tasaRepository.save(tasa);
        log.info("Tasa actualizada exitosamente con ID: {}", id);
        return convertirAResponse(tasaActualizada);
    }

    /**
     * Registrar pago de tasa
     */
    public TasaResponse registrarPago(Long tasaId, BigDecimal montoPago, String usuario) {
        log.info("Registrando pago de {} para tasa ID: {}", montoPago, tasaId);

        Tasa tasa = tasaRepository.findById(tasaId)
                .orElseThrow(() -> new TasaException("Tasa no encontrada con ID: " + tasaId));

        if (tasa.getEstado() == EstadoTasa.ANULADA) {
            throw new TasaException("No se puede registrar pago en una tasa anulada");
        }

        // Calcular nuevo monto pagado
        BigDecimal nuevoMontoPagado = tasa.getMontoPagado().add(montoPago);
        
        if (nuevoMontoPagado.compareTo(tasa.getMontoBase()) > 0) {
            throw new TasaException("El monto del pago excede el saldo pendiente de la tasa");
        }

        // Actualizar monto pagado
        tasa.setMontoPagado(nuevoMontoPagado);
        
        // Actualizar estado según el pago
        if (nuevoMontoPagado.compareTo(tasa.getMontoBase()) == 0) {
            tasa.setEstado(EstadoTasa.PAGADA);
            tasa.setFechaPago(LocalDateTime.now());
        } else {
            tasa.setEstado(EstadoTasa.PARCIALMENTE_PAGADA);
        }

        tasa.setUsuarioModificacion(usuario);
        tasa.setFechaModificacion(LocalDateTime.now());

        Tasa tasaActualizada = tasaRepository.save(tasa);
        log.info("Pago registrado exitosamente para tasa ID: {}", tasaId);
        return convertirAResponse(tasaActualizada);
    }

    /**
     * Cambiar estado de tasa
     */
    public TasaResponse cambiarEstado(Long tasaId, EstadoTasa nuevoEstado, String usuario) {
        log.info("Cambiando estado de tasa ID: {} a {}", tasaId, nuevoEstado);

        Tasa tasa = tasaRepository.findById(tasaId)
                .orElseThrow(() -> new TasaException("Tasa no encontrada con ID: " + tasaId));

        tasa.setEstado(nuevoEstado);
        tasa.setUsuarioModificacion(usuario);
        tasa.setFechaModificacion(LocalDateTime.now());

        if (nuevoEstado == EstadoTasa.ANULADA) {
            tasa.setObservaciones((tasa.getObservaciones() != null ? tasa.getObservaciones() + "\n" : "") 
                    + "Tasa anulada el " + LocalDateTime.now() + " por " + usuario);
        }

        Tasa tasaActualizada = tasaRepository.save(tasa);
        log.info("Estado cambiado exitosamente para tasa ID: {}", tasaId);
        return convertirAResponse(tasaActualizada);
    }

    /**
     * Obtener estadísticas de tasas
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Conteos por estado
        List<Object[]> conteosPorEstado = tasaRepository.countTasasByEstado();
        Map<String, Long> estadoStats = new HashMap<>();
        for (Object[] row : conteosPorEstado) {
            estadoStats.put(((EstadoTasa) row[0]).getDescripcion(), (Long) row[1]);
        }
        estadisticas.put("porEstado", estadoStats);

        // Conteos por tipo
        List<Object[]> conteosPorTipo = tasaRepository.countTasasByTipo();
        Map<String, Long> tipoStats = new HashMap<>();
        for (Object[] row : conteosPorTipo) {
            tipoStats.put(((TipoTasa) row[0]).getDescripcion(), (Long) row[1]);
        }
        estadisticas.put("porTipo", tipoStats);

        // Totales financieros
        BigDecimal totalRecaudado = tasaRepository.getTotalRecaudado();
        BigDecimal totalPendiente = tasaRepository.getTotalPendiente();
        
        estadisticas.put("totalRecaudado", totalRecaudado != null ? totalRecaudado : BigDecimal.ZERO);
        estadisticas.put("totalPendiente", totalPendiente != null ? totalPendiente : BigDecimal.ZERO);
        estadisticas.put("totalTasas", tasaRepository.count());

        return estadisticas;
    }

    /**
     * Actualizar tasas vencidas
     */
    @Transactional
    public void actualizarTasasVencidas() {
        List<EstadoTasa> estadosActivos = List.of(EstadoTasa.ACTIVA, EstadoTasa.PARCIALMENTE_PAGADA);
        List<Tasa> tasasVencidas = tasaRepository.findTasasVencidas(LocalDateTime.now(), estadosActivos);
        
        for (Tasa tasa : tasasVencidas) {
            tasa.setEstado(EstadoTasa.VENCIDA);
            tasa.setFechaModificacion(LocalDateTime.now());
        }
        
        if (!tasasVencidas.isEmpty()) {
            tasaRepository.saveAll(tasasVencidas);
            log.info("Actualizadas {} tasas a estado VENCIDA", tasasVencidas.size());
        }
    }

    /**
     * Generar número de tasa único
     */
    private String generarNumeroTasa(TipoTasa tipoTasa) {
        String prefijo = "TAS-" + LocalDateTime.now().getYear() + 
                        String.format("%02d", LocalDateTime.now().getMonthValue()) + "-";
        
        // Buscar el último número para este mes
        String patron = prefijo + "%";
        long count = tasaRepository.count() + 1;
        
        String numeroTasa;
        do {
            numeroTasa = prefijo + String.format("%04d", count);
            count++;
        } while (tasaRepository.existsByNumeroTasa(numeroTasa));
        
        return numeroTasa;
    }

    /**
     * Convertir entidad Tasa a TasaResponse
     */
    private TasaResponse convertirAResponse(Tasa tasa) {
        long diasVencimiento = 0;
        if (tasa.getFechaVencimiento() != null) {
            diasVencimiento = ChronoUnit.DAYS.between(LocalDateTime.now(), tasa.getFechaVencimiento());
        }

        return TasaResponse.builder()
                .id(tasa.getId())
                .contribuyenteId(tasa.getContribuyente().getId())
                .contribuyenteNombre(obtenerNombreContribuyente(tasa.getContribuyente()))
                .contribuyenteRif(tasa.getContribuyente().getRif())
                .numeroTasa(tasa.getNumeroTasa())
                .tipoTasa(tasa.getTipoTasa())
                .tipoTasaDescripcion(tasa.getTipoTasa().getDescripcion())
                .descripcion(tasa.getDescripcion())
                .montoBase(tasa.getMontoBase())
                .montoPagado(tasa.getMontoPagado())
                .saldoPendiente(tasa.getSaldoPendiente())
                .estado(tasa.getEstado())
                .estadoDescripcion(tasa.getEstado().getDescripcion())
                .periodoFacturacion(tasa.getPeriodoFacturacion())
                .periodoFacturacionDescripcion(tasa.getPeriodoFacturacion().getDescripcion())
                .fechaInicio(tasa.getFechaInicio())
                .fechaVencimiento(tasa.getFechaVencimiento())
                .fechaPago(tasa.getFechaPago())
                .direccion(tasa.getDireccion())
                .zonaMunicipal(tasa.getZonaMunicipal())
                .areaInmueble(tasa.getAreaInmueble())
                .valorCatastral(tasa.getValorCatastral())
                .observaciones(tasa.getObservaciones())
                .fechaRegistro(tasa.getFechaRegistro())
                .usuarioRegistro(tasa.getUsuarioRegistro())
                .vencida(tasa.isVencida())
                .completamentePagada(tasa.isCompletamentePagada())
                .diasVencimiento(diasVencimiento)
                .build();
    }

    /**
     * Eliminar una tasa
     */
    public void eliminarTasa(Long id) {
        log.info("Eliminando tasa con ID: {}", id);
        
        Tasa tasa = tasaRepository.findById(id)
                .orElseThrow(() -> new TasaException("Tasa no encontrada con ID: " + id));
        
        // Verificar que la tasa no esté pagada
        if (tasa.getEstado() == EstadoTasa.PAGADA) {
            throw new TasaException("No se puede eliminar una tasa que ya está pagada");
        }
        
        tasaRepository.delete(tasa);
        log.info("Tasa eliminada exitosamente: {}", id);
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
