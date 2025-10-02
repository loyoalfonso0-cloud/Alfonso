package com.example.demo.tributario.service;

import com.example.demo.tributario.model.Impuesto;
import com.example.demo.tributario.repository.ImpuestoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ImpuestoService {
    
    private final ImpuestoRepository impuestoRepository;

    public List<Impuesto> listarTodos() {
        return impuestoRepository.findAll();
    }

    public Optional<Impuesto> buscarPorId(Long id) {
        return impuestoRepository.findById(id);
    }

    public Optional<Impuesto> buscarPorCodigo(String codigo) {
        return impuestoRepository.findByCodigo(codigo);
    }

    public Impuesto crear(Impuesto impuesto) {
        // Validar que el código no exista
        if (impuestoRepository.findByCodigo(impuesto.getCodigo()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un impuesto con el código: " + impuesto.getCodigo());
        }
        
        // Validar tasa
        if (impuesto.getTasa().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La tasa no puede ser negativa");
        }
        
        if (impuesto.getTasa().compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("La tasa no puede ser mayor al 100%");
        }
        
        return impuestoRepository.save(impuesto);
    }

    public Impuesto actualizar(Long id, Impuesto impuestoActualizado) {
        Impuesto impuestoExistente = impuestoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Impuesto no encontrado con ID: " + id));

        // Validar que el código no esté siendo usado por otro impuesto
        Optional<Impuesto> impuestoConCodigo = impuestoRepository.findByCodigo(impuestoActualizado.getCodigo());
        if (impuestoConCodigo.isPresent() && !impuestoConCodigo.get().getId().equals(id)) {
            throw new IllegalArgumentException("Ya existe otro impuesto con el código: " + impuestoActualizado.getCodigo());
        }

        // Validar tasa
        if (impuestoActualizado.getTasa().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La tasa no puede ser negativa");
        }
        
        if (impuestoActualizado.getTasa().compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("La tasa no puede ser mayor al 100%");
        }

        // Actualizar campos
        impuestoExistente.setCodigo(impuestoActualizado.getCodigo());
        impuestoExistente.setNombre(impuestoActualizado.getNombre());
        impuestoExistente.setTasa(impuestoActualizado.getTasa());

        return impuestoRepository.save(impuestoExistente);
    }

    public void eliminar(Long id) {
        if (!impuestoRepository.existsById(id)) {
            throw new IllegalArgumentException("Impuesto no encontrado con ID: " + id);
        }
        
        try {
            impuestoRepository.deleteById(id);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalArgumentException("No se puede eliminar el impuesto porque está siendo utilizado en declaraciones o comprobantes.");
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al eliminar el impuesto: " + e.getMessage());
        }
    }

    public Map<String, Object> obtenerEstadisticas() {
        long total = impuestoRepository.count();
        
        // Calcular tasa promedio
        List<Impuesto> impuestos = impuestoRepository.findAll();
        BigDecimal tasaPromedio = impuestos.stream()
            .map(Impuesto::getTasa)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(Math.max(1, total)), 2, java.math.RoundingMode.HALF_UP);
        
        // Contar impuestos activos (con tasa > 0)
        long activos = impuestos.stream()
            .mapToLong(i -> i.getTasa().compareTo(BigDecimal.ZERO) > 0 ? 1 : 0)
            .sum();
        
        return Map.of(
            "total", total,
            "activos", activos,
            "tasaPromedio", tasaPromedio.doubleValue()
        );
    }
}
