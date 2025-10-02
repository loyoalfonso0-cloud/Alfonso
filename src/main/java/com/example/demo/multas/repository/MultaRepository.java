package com.example.demo.multas.repository;

import com.example.demo.multas.model.EstadoMulta;
import com.example.demo.multas.model.Multa;
import com.example.demo.multas.model.TipoInfraccion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MultaRepository extends JpaRepository<Multa, Long> {
    
    // Buscar por número de multa
    Optional<Multa> findByNumeroMulta(String numeroMulta);
    
    // Buscar por contribuyente
    Page<Multa> findByContribuyenteId(Long contribuyenteId, Pageable pageable);
    
    // Buscar por estado
    Page<Multa> findByEstado(EstadoMulta estado, Pageable pageable);
    
    // Buscar por tipo de infracción
    Page<Multa> findByTipoInfraccion(TipoInfraccion tipoInfraccion, Pageable pageable);
    
    // Buscar multas vencidas
    @Query("SELECT m FROM Multa m WHERE m.fechaVencimiento < :fecha AND m.estado IN :estados")
    List<Multa> findMultasVencidas(@Param("fecha") LocalDateTime fecha, @Param("estados") List<EstadoMulta> estados);
    
    // Buscar por rango de fechas
    @Query("SELECT m FROM Multa m WHERE m.fechaInfraccion BETWEEN :fechaInicio AND :fechaFin")
    Page<Multa> findByFechaInfraccionBetween(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                           @Param("fechaFin") LocalDateTime fechaFin, 
                                           Pageable pageable);
    
    // Estadísticas
    @Query("SELECT COUNT(m) FROM Multa m WHERE m.estado = :estado")
    Long countByEstado(@Param("estado") EstadoMulta estado);
    
    @Query("SELECT SUM(m.monto) FROM Multa m WHERE m.estado = :estado")
    BigDecimal sumMontoByEstado(@Param("estado") EstadoMulta estado);
    
    @Query("SELECT SUM(m.montoPagado) FROM Multa m")
    BigDecimal sumMontoPagado();
    
    // Buscar multas del día
    @Query("SELECT m FROM Multa m WHERE DATE(m.fechaRegistro) = DATE(:fecha)")
    List<Multa> findMultasDelDia(@Param("fecha") LocalDateTime fecha);
    
    // Buscar multas por contribuyente y estado
    List<Multa> findByContribuyenteIdAndEstado(Long contribuyenteId, EstadoMulta estado);
    
    // Verificar si existe multa con número
    boolean existsByNumeroMulta(String numeroMulta);
    
    // Buscar multas próximas a vencer
    @Query("SELECT m FROM Multa m WHERE m.fechaVencimiento BETWEEN :fechaInicio AND :fechaFin AND m.estado IN :estados")
    List<Multa> findMultasProximasAVencer(@Param("fechaInicio") LocalDateTime fechaInicio,
                                        @Param("fechaFin") LocalDateTime fechaFin,
                                        @Param("estados") List<EstadoMulta> estados);
}
