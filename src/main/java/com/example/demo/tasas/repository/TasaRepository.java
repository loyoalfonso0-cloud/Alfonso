package com.example.demo.tasas.repository;

import com.example.demo.tasas.model.EstadoTasa;
import com.example.demo.tasas.model.Tasa;
import com.example.demo.tasas.model.TipoTasa;
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

/**
 * Repositorio para la gestión de tasas municipales
 */
@Repository
public interface TasaRepository extends JpaRepository<Tasa, Long> {

    // Buscar por número de tasa
    Optional<Tasa> findByNumeroTasa(String numeroTasa);

    // Verificar si existe un número de tasa
    boolean existsByNumeroTasa(String numeroTasa);

    // Buscar por contribuyente
    List<Tasa> findByContribuyenteId(Long contribuyenteId);
    Page<Tasa> findByContribuyenteId(Long contribuyenteId, Pageable pageable);

    // Buscar por estado
    List<Tasa> findByEstado(EstadoTasa estado);
    Page<Tasa> findByEstado(EstadoTasa estado, Pageable pageable);

    // Buscar por tipo de tasa
    List<Tasa> findByTipoTasa(TipoTasa tipoTasa);
    Page<Tasa> findByTipoTasa(TipoTasa tipoTasa, Pageable pageable);

    // Buscar por zona municipal
    List<Tasa> findByZonaMunicipal(String zonaMunicipal);

    // Buscar tasas vencidas
    @Query("SELECT t FROM Tasa t WHERE t.fechaVencimiento < :fecha AND t.estado IN :estados")
    List<Tasa> findTasasVencidas(@Param("fecha") LocalDateTime fecha, @Param("estados") List<EstadoTasa> estados);

    // Buscar tasas por rango de fechas
    @Query("SELECT t FROM Tasa t WHERE t.fechaInicio BETWEEN :fechaInicio AND :fechaFin")
    List<Tasa> findByFechaInicioBetween(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                        @Param("fechaFin") LocalDateTime fechaFin);

    // Buscar tasas con saldo pendiente
    @Query("SELECT t FROM Tasa t WHERE (t.montoBase - t.montoPagado) > 0")
    List<Tasa> findTasasConSaldoPendiente();

    // Estadísticas - Total de tasas por estado
    @Query("SELECT t.estado, COUNT(t) FROM Tasa t GROUP BY t.estado")
    List<Object[]> countTasasByEstado();

    // Estadísticas - Total de tasas por tipo
    @Query("SELECT t.tipoTasa, COUNT(t) FROM Tasa t GROUP BY t.tipoTasa")
    List<Object[]> countTasasByTipo();

    // Estadísticas - Monto total recaudado
    @Query("SELECT SUM(t.montoPagado) FROM Tasa t")
    BigDecimal getTotalRecaudado();

    // Estadísticas - Monto total pendiente
    @Query("SELECT SUM(t.montoBase - t.montoPagado) FROM Tasa t WHERE (t.montoBase - t.montoPagado) > 0")
    BigDecimal getTotalPendiente();

    // Estadísticas - Recaudación por tipo de tasa
    @Query("SELECT t.tipoTasa, SUM(t.montoPagado) FROM Tasa t GROUP BY t.tipoTasa")
    List<Object[]> getRecaudacionByTipo();

    // Estadísticas - Recaudación por zona
    @Query("SELECT t.zonaMunicipal, SUM(t.montoPagado) FROM Tasa t WHERE t.zonaMunicipal IS NOT NULL GROUP BY t.zonaMunicipal")
    List<Object[]> getRecaudacionByZona();

    // Buscar tasas próximas a vencer
    @Query("SELECT t FROM Tasa t WHERE t.fechaVencimiento BETWEEN :fechaInicio AND :fechaFin AND t.estado IN :estados")
    List<Tasa> findTasasProximasAVencer(@Param("fechaInicio") LocalDateTime fechaInicio,
                                        @Param("fechaFin") LocalDateTime fechaFin,
                                        @Param("estados") List<EstadoTasa> estados);

    // Buscar por múltiples criterios
    @Query("SELECT t FROM Tasa t WHERE " +
           "(:contribuyenteId IS NULL OR t.contribuyente.id = :contribuyenteId) AND " +
           "(:tipoTasa IS NULL OR t.tipoTasa = :tipoTasa) AND " +
           "(:estado IS NULL OR t.estado = :estado) AND " +
           "(:zonaMunicipal IS NULL OR t.zonaMunicipal LIKE %:zonaMunicipal%)")
    Page<Tasa> findByMultiplesCriterios(@Param("contribuyenteId") Long contribuyenteId,
                                        @Param("tipoTasa") TipoTasa tipoTasa,
                                        @Param("estado") EstadoTasa estado,
                                        @Param("zonaMunicipal") String zonaMunicipal,
                                        Pageable pageable);

    // Contar tasas por contribuyente
    long countByContribuyenteId(Long contribuyenteId);

    // Buscar tasas de un contribuyente por estado
    List<Tasa> findByContribuyenteIdAndEstado(Long contribuyenteId, EstadoTasa estado);
}
