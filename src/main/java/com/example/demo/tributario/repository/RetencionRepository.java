package com.example.demo.tributario.repository;

import com.example.demo.tributario.model.Retencion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

public interface RetencionRepository extends JpaRepository<Retencion, Long> {
    
    List<Retencion> findByContribuyenteId(Long contribuyenteId);
    
    List<Retencion> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);
    
    List<Retencion> findByEstado(Retencion.EstadoRetencion estado);
    
    // Consulta más segura que evita problemas con enums
    @Query("SELECT r FROM Retencion r WHERE r.estado = :estado")
    List<Retencion> findByEstadoSeguro(@Param("estado") Retencion.EstadoRetencion estado);
    
    // Consulta para obtener el monto total de retenciones aplicadas
    @Query("SELECT COALESCE(SUM(r.montoRetenido), 0) FROM Retencion r WHERE r.estado = :estado")
    BigDecimal sumMontoRetenidoAplicado(@Param("estado") Retencion.EstadoRetencion estado);
    
    // Consulta para obtener retenciones por contribuyente y estado
    @Query("SELECT r FROM Retencion r WHERE r.contribuyente.id = :contribuyenteId AND r.estado = :estado")
    List<Retencion> findByContribuyenteIdAndEstado(@Param("contribuyenteId") Long contribuyenteId, 
                                                   @Param("estado") Retencion.EstadoRetencion estado);
    
    @Query("SELECT r FROM Retencion r WHERE r.contribuyente.razonSocial LIKE %:termino% OR r.concepto LIKE %:termino%")
    List<Retencion> buscarPorTermino(@Param("termino") String termino);
    
    @Query("SELECT COUNT(r) FROM Retencion r WHERE YEAR(r.fecha) = YEAR(CURRENT_DATE) AND MONTH(r.fecha) = MONTH(CURRENT_DATE)")
    long countCreatedThisMonth();
    
    // Método completamente seguro
    @Query("SELECT r FROM Retencion r WHERE r.estado = 'APLICADA'")
    List<Retencion> findByEstadoAplicada();
    
    // Métodos para estadísticas sin usar enum en consulta
    @Query("SELECT COUNT(r) FROM Retencion r")
    long countTotal();
    
    @Query("SELECT COUNT(r) FROM Retencion r WHERE YEAR(r.fecha) = YEAR(CURRENT_DATE) AND MONTH(r.fecha) = MONTH(CURRENT_DATE)")
    long countEsteMes();
}
