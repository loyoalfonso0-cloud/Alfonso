package com.example.demo.tributario.repository;

import com.example.demo.tributario.model.Contribuyente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContribuyenteRepository extends JpaRepository<Contribuyente, Long> {
    Optional<Contribuyente> findByRif(String rif);
    
    List<Contribuyente> findByActivoTrue();
    
    List<Contribuyente> findByTipoContribuyente(Contribuyente.TipoContribuyente tipo);
    
    @Query("SELECT c FROM Contribuyente c WHERE " +
           "LOWER(c.razonSocial) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(c.rif) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Contribuyente> buscarPorTermino(@Param("termino") String termino);
    
    @Query("SELECT c FROM Contribuyente c WHERE " +
           "(LOWER(c.razonSocial) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(c.rif) LIKE LOWER(CONCAT('%', :termino, '%'))) AND " +
           "(:tipo IS NULL OR c.tipoContribuyente = :tipo) AND " +
           "(:activo IS NULL OR c.activo = :activo)")
    Page<Contribuyente> buscarConFiltros(@Param("termino") String termino, 
                                        @Param("tipo") Contribuyente.TipoContribuyente tipo,
                                        @Param("activo") Boolean activo,
                                        Pageable pageable);
    
    long countByActivoTrue();
    
    @Query("SELECT COUNT(c) FROM Contribuyente c WHERE MONTH(c.creadoEn) = MONTH(CURRENT_DATE) AND YEAR(c.creadoEn) = YEAR(CURRENT_DATE)")
    long countCreatedThisMonth();
}


