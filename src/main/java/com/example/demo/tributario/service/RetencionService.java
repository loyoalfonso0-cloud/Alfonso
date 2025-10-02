package com.example.demo.tributario.service;

import com.example.demo.tributario.model.Retencion;
import com.example.demo.tributario.model.Contribuyente;
import com.example.demo.tributario.repository.RetencionRepository;
import com.example.demo.tributario.repository.ContribuyenteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetencionService {
    
    private final RetencionRepository retencionRepository;
    private final ContribuyenteRepository contribuyenteRepository;
    
    public List<Retencion> obtenerTodas() {
        try {
            return retencionRepository.findAll();
        } catch (Exception e) {
            log.error("Error al obtener todas las retenciones", e);
            return List.of();
        }
    }
    
    public Optional<Retencion> obtenerPorId(Long id) {
        try {
            return retencionRepository.findById(id);
        } catch (Exception e) {
            log.error("Error al obtener retención con id: {}", id, e);
            return Optional.empty();
        }
    }
    
    @Transactional
    public Retencion guardar(Retencion retencion) {
        try {
            // Validar que el contribuyente existe
            if (retencion.getContribuyente() != null && retencion.getContribuyente().getId() != null) {
                Optional<Contribuyente> contribuyente = contribuyenteRepository.findById(retencion.getContribuyente().getId());
                if (contribuyente.isPresent()) {
                    retencion.setContribuyente(contribuyente.get());
                }
            }
            
            // Calcular monto retenido automáticamente si no está definido o es cero
            if (retencion.getMontoRetenido() == null || retencion.getMontoRetenido().compareTo(BigDecimal.ZERO) <= 0) {
                if (retencion.getMontoBase() != null && retencion.getPorcentaje() != null) {
                    BigDecimal montoCalculado = retencion.getMontoBase()
                            .multiply(retencion.getPorcentaje())
                            .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                    retencion.setMontoRetenido(montoCalculado);
                }
            }
            
            // Establecer estado por defecto si no está definido
            if (retencion.getEstado() == null) {
                retencion.setEstado(Retencion.EstadoRetencion.PENDIENTE);
            }
            
            return retencionRepository.save(retencion);
        } catch (Exception e) {
            log.error("Error al guardar retención", e);
            throw new RuntimeException("Error al guardar la retención: " + e.getMessage());
        }
    }
    
    @Transactional
    public void eliminar(Long id) {
        if (!retencionRepository.existsById(id)) {
            throw new IllegalArgumentException("Retención no encontrada con ID: " + id);
        }
        
        try {
            retencionRepository.deleteById(id);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalArgumentException("No se puede eliminar la retención porque está siendo utilizada en declaraciones o comprobantes.");
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al eliminar la retención: " + e.getMessage());
        }
    }
    
    public List<Retencion> obtenerPorContribuyente(Long contribuyenteId) {
        try {
            return retencionRepository.findByContribuyenteId(contribuyenteId);
        } catch (Exception e) {
            log.error("Error al obtener retenciones del contribuyente: {}", contribuyenteId, e);
            return List.of();
        }
    }
    
    public List<Retencion> obtenerPorFecha(LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            return retencionRepository.findByFechaBetween(fechaInicio, fechaFin);
        } catch (Exception e) {
            log.error("Error al obtener retenciones por fecha", e);
            return List.of();
        }
    }
    
    public List<Retencion> obtenerPorEstado(Retencion.EstadoRetencion estado) {
        try {
            return retencionRepository.findByEstadoSeguro(estado);
        } catch (Exception e) {
            log.error("Error al obtener retenciones por estado: {}", estado, e);
            return List.of();
        }
    }
    
    public BigDecimal obtenerMontoTotalAplicado() {
        try {
            return retencionRepository.sumMontoRetenidoAplicado(Retencion.EstadoRetencion.APLICADA);
        } catch (Exception e) {
            log.error("Error al obtener monto total aplicado", e);
            return BigDecimal.ZERO;
        }
    }
    
    public List<Retencion> obtenerPorContribuyenteYEstado(Long contribuyenteId, Retencion.EstadoRetencion estado) {
        try {
            return retencionRepository.findByContribuyenteIdAndEstado(contribuyenteId, estado);
        } catch (Exception e) {
            log.error("Error al obtener retenciones del contribuyente {} con estado {}", contribuyenteId, estado, e);
            return List.of();
        }
    }
}
