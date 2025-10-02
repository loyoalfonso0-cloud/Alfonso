package com.example.demo.tributario.service;

import com.example.demo.tributario.model.Contribuyente;
import com.example.demo.tributario.repository.ContribuyenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ContribuyenteService {
    
    private final ContribuyenteRepository contribuyenteRepository;

    public List<Contribuyente> listarTodos() {
        return contribuyenteRepository.findAll();
    }

    public List<Contribuyente> listarActivos() {
        return contribuyenteRepository.findByActivoTrue();
    }

    public Optional<Contribuyente> buscarPorId(Long id) {
        return contribuyenteRepository.findById(id);
    }

    public Optional<Contribuyente> buscarPorRuc(String ruc) {
        return contribuyenteRepository.findByRif(ruc);
    }

    public List<Contribuyente> buscarPorTermino(String termino) {
        return contribuyenteRepository.buscarPorTermino(termino);
    }

    public Page<Contribuyente> buscarConFiltros(String termino, Contribuyente.TipoContribuyente tipo, 
                                               Boolean activo, Pageable pageable) {
        return contribuyenteRepository.buscarConFiltros(termino, tipo, activo, pageable);
    }

    public Contribuyente crear(Contribuyente contribuyente) {
        // Validar que el RUC no exista
        if (contribuyenteRepository.findByRif(contribuyente.getRif()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un contribuyente con el RIF: " + contribuyente.getRif());
        }
        
        return contribuyenteRepository.save(contribuyente);
    }

    public Contribuyente actualizar(Long id, Contribuyente contribuyenteActualizado) {
        Contribuyente contribuyenteExistente = contribuyenteRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Contribuyente no encontrado con ID: " + id));

        // Validar que el RUC no esté siendo usado por otro contribuyente
        Optional<Contribuyente> contribuyenteConRif = contribuyenteRepository.findByRif(contribuyenteActualizado.getRif());
        if (contribuyenteConRif.isPresent() && !contribuyenteConRif.get().getId().equals(id)) {
            throw new IllegalArgumentException("Ya existe otro contribuyente con el RIF: " + contribuyenteActualizado.getRif());
        }

        // Actualizar campos (excepto el estado activo que se maneja por separado)
        contribuyenteExistente.setRif(contribuyenteActualizado.getRif());
        contribuyenteExistente.setRazonSocial(contribuyenteActualizado.getRazonSocial());
        contribuyenteExistente.setDireccion(contribuyenteActualizado.getDireccion());
        contribuyenteExistente.setTelefono(contribuyenteActualizado.getTelefono());
        contribuyenteExistente.setEmail(contribuyenteActualizado.getEmail());
        contribuyenteExistente.setTipoContribuyente(contribuyenteActualizado.getTipoContribuyente());
        contribuyenteExistente.setRepresentanteLegal(contribuyenteActualizado.getRepresentanteLegal());

        return contribuyenteRepository.save(contribuyenteExistente);
    }

    public void cambiarEstado(Long id, Boolean nuevoEstado) {
        Contribuyente contribuyente = contribuyenteRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Contribuyente no encontrado con ID: " + id));
        
        contribuyente.setActivo(nuevoEstado);
        contribuyenteRepository.save(contribuyente);
    }


    public Map<String, Object> obtenerEstadisticas() {
        long total = contribuyenteRepository.count();
        long activos = contribuyenteRepository.countByActivoTrue();
        long esteMes = contribuyenteRepository.countCreatedThisMonth();
        
        return Map.of(
            "total", total,
            "activos", activos,
            "inactivos", total - activos,
            "esteMes", esteMes
        );
    }

    public List<Contribuyente> listarPorTipo(Contribuyente.TipoContribuyente tipo) {
        return contribuyenteRepository.findByTipoContribuyente(tipo);
    }
}
