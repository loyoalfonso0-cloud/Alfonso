package com.example.demo.tributario.service;

import com.example.demo.tributario.model.Comprobante;
import com.example.demo.tributario.repository.ComprobanteRepository;
import com.example.demo.tributario.repository.ContribuyenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ComprobanteService {
    
    private final ComprobanteRepository comprobanteRepository;
    private final ContribuyenteRepository contribuyenteRepository;

    public List<Comprobante> listarTodos() {
        return comprobanteRepository.findAll();
    }

    public Optional<Comprobante> buscarPorId(Long id) {
        return comprobanteRepository.findById(id);
    }

    public List<Comprobante> buscarPorContribuyente(Long contribuyenteId) {
        return comprobanteRepository.findByContribuyenteId(contribuyenteId);
    }

    public List<Comprobante> buscarPorTipo(String tipo) {
        return comprobanteRepository.findByTipo(tipo);
    }

    public List<Comprobante> buscarPorEstado(Comprobante.EstadoComprobante estado) {
        return comprobanteRepository.findByEstado(estado);
    }

    public List<Comprobante> buscarPorTermino(String termino) {
        return comprobanteRepository.buscarPorTermino(termino);
    }

    public List<Comprobante> buscarPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return comprobanteRepository.findByFechaEmisionBetween(fechaInicio, fechaFin);
    }

    public Comprobante crear(Comprobante comprobante) {
        // Validar que el contribuyente existe
        if (comprobante.getContribuyente() != null && comprobante.getContribuyente().getId() != null) {
            contribuyenteRepository.findById(comprobante.getContribuyente().getId())
                .orElseThrow(() -> new IllegalArgumentException("Contribuyente no encontrado"));
        }
        
        // Validar que no exista otro comprobante con el mismo tipo, serie y número
        Optional<Comprobante> existente = comprobanteRepository.findByTipoAndSerieAndNumero(
            comprobante.getTipo(), comprobante.getSerie(), comprobante.getNumero());
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe un comprobante con el mismo tipo, serie y número");
        }
        
        // Calcular total si no está establecido
        if (comprobante.getTotal() == null && comprobante.getSubtotal() != null && comprobante.getImpuesto() != null) {
            BigDecimal total = comprobante.getSubtotal().add(comprobante.getImpuesto());
            comprobante.setTotal(total);
        }
        
        return comprobanteRepository.save(comprobante);
    }

    public Comprobante actualizar(Long id, Comprobante comprobanteActualizado) {
        Comprobante comprobanteExistente = comprobanteRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Comprobante no encontrado con ID: " + id));

        // Validar que el contribuyente existe si se está cambiando
        if (comprobanteActualizado.getContribuyente() != null && comprobanteActualizado.getContribuyente().getId() != null) {
            contribuyenteRepository.findById(comprobanteActualizado.getContribuyente().getId())
                .orElseThrow(() -> new IllegalArgumentException("Contribuyente no encontrado"));
        }

        // Actualizar campos
        comprobanteExistente.setContribuyente(comprobanteActualizado.getContribuyente());
        comprobanteExistente.setTipo(comprobanteActualizado.getTipo());
        comprobanteExistente.setSerie(comprobanteActualizado.getSerie());
        comprobanteExistente.setNumero(comprobanteActualizado.getNumero());
        comprobanteExistente.setFechaEmision(comprobanteActualizado.getFechaEmision());
        comprobanteExistente.setSubtotal(comprobanteActualizado.getSubtotal());
        comprobanteExistente.setImpuesto(comprobanteActualizado.getImpuesto());
        comprobanteExistente.setEstado(comprobanteActualizado.getEstado());
        
        // Recalcular total
        BigDecimal total = comprobanteExistente.getSubtotal().add(comprobanteExistente.getImpuesto());
        comprobanteExistente.setTotal(total);

        return comprobanteRepository.save(comprobanteExistente);
    }

    public Comprobante cambiarEstado(Long id, Comprobante.EstadoComprobante nuevoEstado) {
        Comprobante comprobante = comprobanteRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Comprobante no encontrado con ID: " + id));
        
        comprobante.setEstado(nuevoEstado);
        return comprobanteRepository.save(comprobante);
    }

    public void eliminar(Long id) {
        if (!comprobanteRepository.existsById(id)) {
            throw new IllegalArgumentException("Comprobante no encontrado con ID: " + id);
        }
        
        try {
            comprobanteRepository.deleteById(id);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalArgumentException("No se puede eliminar el comprobante porque está siendo utilizado en otras operaciones o declaraciones.");
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al eliminar el comprobante: " + e.getMessage());
        }
    }

    public Map<String, Object> obtenerEstadisticas() {
        long total = comprobanteRepository.count();
        long esteMes = comprobanteRepository.countCreatedThisMonth();
        long vencidos = comprobanteRepository.countPorEstado(Comprobante.EstadoComprobante.VENCIDO);
        BigDecimal montoTotal = comprobanteRepository.sumTotalPorEstado(Comprobante.EstadoComprobante.EMITIDO);
        
        if (montoTotal == null) {
            montoTotal = BigDecimal.ZERO;
        }
        
        return Map.of(
            "total", total,
            "esteMes", esteMes,
            "vencidos", vencidos,
            "montoTotal", montoTotal.doubleValue()
        );
    }
}
