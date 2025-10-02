package com.example.demo.transacciones.repository;

import com.example.demo.transacciones.model.EstadoTransaccion;
import com.example.demo.transacciones.model.TipoTransaccion;
import com.example.demo.transacciones.model.Transaccion;
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
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    // Buscar por número de transacción
    Optional<Transaccion> findByNumeroTransaccion(String numeroTransaccion);

    // Buscar por contribuyente
    Page<Transaccion> findByContribuyenteId(Long contribuyenteId, Pageable pageable);

    // Buscar por estado
    Page<Transaccion> findByEstado(EstadoTransaccion estado, Pageable pageable);

    // Buscar por tipo de transacción
    Page<Transaccion> findByTipoTransaccion(TipoTransaccion tipoTransaccion, Pageable pageable);

    // Buscar por rango de fechas
    @Query("SELECT t FROM Transaccion t WHERE t.fechaTransaccion BETWEEN :fechaInicio AND :fechaFin ORDER BY t.fechaTransaccion DESC")
    Page<Transaccion> findByFechaTransaccionBetween(
        @Param("fechaInicio") LocalDateTime fechaInicio, 
        @Param("fechaFin") LocalDateTime fechaFin, 
        Pageable pageable
    );

    // Buscar por referencia externa
    List<Transaccion> findByReferenciaExterna(String referenciaExterna);

    // Buscar por entidad relacionada
    @Query("SELECT t FROM Transaccion t WHERE t.entidadRelacionadaTipo = :tipo AND t.entidadRelacionadaId = :id")
    List<Transaccion> findByEntidadRelacionada(@Param("tipo") String tipo, @Param("id") Long id);

    // Estadísticas - Total de transacciones
    @Query("SELECT COUNT(t) FROM Transaccion t")
    Long countTotalTransacciones();

    // Estadísticas - Total por estado
    @Query("SELECT COUNT(t) FROM Transaccion t WHERE t.estado = :estado")
    Long countByEstado(@Param("estado") EstadoTransaccion estado);

    // Estadísticas - Total por tipo
    @Query("SELECT COUNT(t) FROM Transaccion t WHERE t.tipoTransaccion = :tipo")
    Long countByTipoTransaccion(@Param("tipo") TipoTransaccion tipo);

    // Estadísticas - Suma de montos por estado
    @Query("SELECT COALESCE(SUM(t.monto), 0) FROM Transaccion t WHERE t.estado = :estado")
    BigDecimal sumMontoByEstado(@Param("estado") EstadoTransaccion estado);

    // Estadísticas - Suma de montos por tipo
    @Query("SELECT COALESCE(SUM(t.monto), 0) FROM Transaccion t WHERE t.tipoTransaccion = :tipo")
    BigDecimal sumMontoByTipoTransaccion(@Param("tipo") TipoTransaccion tipo);

    // Estadísticas - Ingresos del día
    @Query("SELECT COALESCE(SUM(t.monto), 0) FROM Transaccion t WHERE " +
           "t.tipoTransaccion IN ('INGRESO', 'PAGO_SERVICIO', 'PAGO_IMPUESTO', 'PAGO_TASA', 'PAGO_MULTA') " +
           "AND DATE(t.fechaTransaccion) = CURRENT_DATE " +
           "AND t.estado IN ('PROCESADA', 'CONFIRMADA')")
    BigDecimal sumIngresosHoy();

    // Estadísticas - Egresos del día
    @Query("SELECT COALESCE(SUM(t.monto), 0) FROM Transaccion t WHERE " +
           "t.tipoTransaccion IN ('EGRESO', 'DEVOLUCION', 'COMISION') " +
           "AND DATE(t.fechaTransaccion) = CURRENT_DATE " +
           "AND t.estado IN ('PROCESADA', 'CONFIRMADA')")
    BigDecimal sumEgresosHoy();

    // Estadísticas - Balance del mes
    @Query("SELECT COALESCE(SUM(CASE WHEN t.tipoTransaccion IN ('INGRESO', 'PAGO_SERVICIO', 'PAGO_IMPUESTO', 'PAGO_TASA', 'PAGO_MULTA') THEN t.monto ELSE -t.monto END), 0) " +
           "FROM Transaccion t WHERE " +
           "YEAR(t.fechaTransaccion) = YEAR(CURRENT_DATE) " +
           "AND MONTH(t.fechaTransaccion) = MONTH(CURRENT_DATE) " +
           "AND t.estado IN ('PROCESADA', 'CONFIRMADA')")
    BigDecimal balanceMesActual();

    // Transacciones pendientes de procesamiento
    @Query("SELECT t FROM Transaccion t WHERE t.estado = 'PENDIENTE' ORDER BY t.fechaRegistro ASC")
    List<Transaccion> findTransaccionesPendientes();

    // Transacciones por contribuyente y estado
    @Query("SELECT t FROM Transaccion t WHERE t.contribuyente.id = :contribuyenteId AND t.estado = :estado ORDER BY t.fechaTransaccion DESC")
    List<Transaccion> findByContribuyenteAndEstado(@Param("contribuyenteId") Long contribuyenteId, @Param("estado") EstadoTransaccion estado);

    // Búsqueda avanzada
    @Query("SELECT t FROM Transaccion t WHERE " +
           "(:numeroTransaccion IS NULL OR t.numeroTransaccion LIKE %:numeroTransaccion%) AND " +
           "(:contribuyenteId IS NULL OR t.contribuyente.id = :contribuyenteId) AND " +
           "(:estado IS NULL OR t.estado = :estado) AND " +
           "(:tipoTransaccion IS NULL OR t.tipoTransaccion = :tipoTransaccion) AND " +
           "(:fechaInicio IS NULL OR t.fechaTransaccion >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR t.fechaTransaccion <= :fechaFin) AND " +
           "(:montoMinimo IS NULL OR t.monto >= :montoMinimo) AND " +
           "(:montoMaximo IS NULL OR t.monto <= :montoMaximo) " +
           "ORDER BY t.fechaTransaccion DESC")
    Page<Transaccion> busquedaAvanzada(
        @Param("numeroTransaccion") String numeroTransaccion,
        @Param("contribuyenteId") Long contribuyenteId,
        @Param("estado") EstadoTransaccion estado,
        @Param("tipoTransaccion") TipoTransaccion tipoTransaccion,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin,
        @Param("montoMinimo") BigDecimal montoMinimo,
        @Param("montoMaximo") BigDecimal montoMaximo,
        Pageable pageable
    );

    // Reporte de transacciones por período
    @Query("SELECT t.tipoTransaccion, COUNT(t), COALESCE(SUM(t.monto), 0) " +
           "FROM Transaccion t WHERE " +
           "t.fechaTransaccion BETWEEN :fechaInicio AND :fechaFin " +
           "AND t.estado IN ('PROCESADA', 'CONFIRMADA') " +
           "GROUP BY t.tipoTransaccion " +
           "ORDER BY SUM(t.monto) DESC")
    List<Object[]> reporteTransaccionesPorPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);

    // Transacciones recientes
    @Query("SELECT t FROM Transaccion t ORDER BY t.fechaRegistro DESC")
    List<Transaccion> findTransaccionesRecientes(Pageable pageable);

    // Verificar si existe número de transacción
    boolean existsByNumeroTransaccion(String numeroTransaccion);
}
