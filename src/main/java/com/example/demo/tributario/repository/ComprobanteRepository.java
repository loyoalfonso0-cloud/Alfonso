package com.example.demo.tributario.repository;

import com.example.demo.tributario.model.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {
    
    List<Comprobante> findByContribuyenteId(Long contribuyenteId);
    
    List<Comprobante> findByTipo(String tipo);
    
    List<Comprobante> findByEstado(Comprobante.EstadoComprobante estado);
    
    List<Comprobante> findByFechaEmisionBetween(LocalDate fechaInicio, LocalDate fechaFin);
    
    Optional<Comprobante> findByTipoAndSerieAndNumero(String tipo, String serie, String numero);
    
    @Query("SELECT c FROM Comprobante c WHERE c.contribuyente.razonSocial LIKE %:termino% OR c.numero LIKE %:termino% OR c.serie LIKE %:termino%")
    List<Comprobante> buscarPorTermino(@Param("termino") String termino);
    
    @Query("SELECT COUNT(c) FROM Comprobante c WHERE YEAR(c.fechaEmision) = YEAR(CURRENT_DATE) AND MONTH(c.fechaEmision) = MONTH(CURRENT_DATE)")
    long countCreatedThisMonth();
    
    @Query("SELECT COALESCE(SUM(c.total), 0) FROM Comprobante c WHERE c.estado = :estado")
    java.math.BigDecimal sumTotalPorEstado(@Param("estado") Comprobante.EstadoComprobante estado);
    
    @Query("SELECT COUNT(c) FROM Comprobante c WHERE c.estado = :estado")
    long countPorEstado(@Param("estado") Comprobante.EstadoComprobante estado);
}
