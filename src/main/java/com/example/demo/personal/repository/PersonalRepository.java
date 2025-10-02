package com.example.demo.personal.repository;

import com.example.demo.personal.model.Personal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PersonalRepository extends JpaRepository<Personal, Long> {
    
    // Buscar por documento
    Optional<Personal> findByDocumento(String documento);
    
    // Buscar empleados activos
    List<Personal> findByActivoTrue();
    
    // Buscar por cargo
    List<Personal> findByCargoContainingIgnoreCase(String cargo);
    
    // Buscar por nombres o apellidos
    @Query("SELECT p FROM Personal p WHERE " +
           "LOWER(p.nombres) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(p.apellidos) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(p.documento) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(p.cargo) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Personal> buscarPorTermino(@Param("termino") String termino);
    
    // Contar empleados activos
    long countByActivoTrue();
    
    // Verificar si existe documento (excluyendo un ID específico para edición)
    @Query("SELECT COUNT(p) > 0 FROM Personal p WHERE p.documento = :documento AND (:id IS NULL OR p.id != :id)")
    boolean existeDocumento(@Param("documento") String documento, @Param("id") Long id);
    
    // Buscar personal por ID de usuario
    Optional<Personal> findByUsuarioId(Long usuarioId);
}


