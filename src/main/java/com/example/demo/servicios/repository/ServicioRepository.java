package com.example.demo.servicios.repository;

import com.example.demo.servicios.domain.EstadoServicio;
import com.example.demo.servicios.domain.Servicio;
import com.example.demo.servicios.domain.TipoServicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Servicio
 */
@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    /**
     * Busca un servicio por su número
     */
    Optional<Servicio> findByNumeroServicio(String numeroServicio);

    /**
     * Busca servicios por contribuyente
     */
    @Query("SELECT s FROM Servicio s WHERE s.contribuyente.id = :contribuyenteId")
    List<Servicio> findByContribuyenteId(@Param("contribuyenteId") Long contribuyenteId);

    /**
     * Busca servicios por estado
     */
    List<Servicio> findByEstado(EstadoServicio estado);

    /**
     * Busca servicios por tipo
     */
    List<Servicio> findByTipoServicio(TipoServicio tipoServicio);

    /**
     * Busca servicios con paginación y filtros
     */
    @Query("SELECT s FROM Servicio s JOIN FETCH s.contribuyente c " +
           "WHERE (:numeroServicio IS NULL OR s.numeroServicio LIKE %:numeroServicio%) " +
           "AND (:contribuyenteNombre IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :contribuyenteNombre, '%')) " +
           "     OR LOWER(c.razonSocial) LIKE LOWER(CONCAT('%', :contribuyenteNombre, '%'))) " +
           "AND (:estado IS NULL OR s.estado = :estado) " +
           "AND (:tipoServicio IS NULL OR s.tipoServicio = :tipoServicio)")
    Page<Servicio> findServiciosConFiltros(
            @Param("numeroServicio") String numeroServicio,
            @Param("contribuyenteNombre") String contribuyenteNombre,
            @Param("estado") EstadoServicio estado,
            @Param("tipoServicio") TipoServicio tipoServicio,
            Pageable pageable);

    /**
     * Cuenta servicios por estado
     */
    @Query("SELECT s.estado, COUNT(s) FROM Servicio s GROUP BY s.estado")
    List<Object[]> countByEstado();

    /**
     * Cuenta servicios por tipo
     */
    @Query("SELECT s.tipoServicio, COUNT(s) FROM Servicio s GROUP BY s.tipoServicio")
    List<Object[]> countByTipoServicio();

    /**
     * Suma total facturado
     */
    @Query("SELECT COALESCE(SUM(s.montoFacturado), 0) FROM Servicio s")
    BigDecimal sumTotalFacturado();

    /**
     * Suma total pagado
     */
    @Query("SELECT COALESCE(SUM(s.montoPagado), 0) FROM Servicio s")
    BigDecimal sumTotalPagado();

    /**
     * Suma saldo pendiente
     */
    @Query("SELECT COALESCE(SUM(s.montoFacturado - s.montoPagado), 0) FROM Servicio s " +
           "WHERE s.montoFacturado > s.montoPagado")
    BigDecimal sumSaldoPendiente();

    /**
     * Servicios vencidos para corte
     */
    @Query("SELECT s FROM Servicio s WHERE s.fechaCorte < :fecha " +
           "AND s.montoFacturado > s.montoPagado " +
           "AND s.estado IN ('ACTIVO', 'SUSPENDIDO')")
    List<Servicio> findServiciosVencidosParaCorte(@Param("fecha") LocalDate fecha);

    /**
     * Servicios que requieren lectura
     */
    @Query("SELECT s FROM Servicio s WHERE s.fechaProximaLectura <= :fecha " +
           "AND s.tipoServicio IN ('AGUA_POTABLE', 'GAS_DOMESTICO') " +
           "AND s.estado = 'ACTIVO'")
    List<Servicio> findServiciosParaLectura(@Param("fecha") LocalDate fecha);

    /**
     * Servicios por rango de fechas
     */
    @Query("SELECT s FROM Servicio s WHERE s.fechaRegistro BETWEEN :fechaInicio AND :fechaFin")
    List<Servicio> findByFechaRegistroBetween(
            @Param("fechaInicio") LocalDate fechaInicio, 
            @Param("fechaFin") LocalDate fechaFin);

    /**
     * Obtiene el siguiente número de secuencia para el año actual
     */
    @Query("SELECT COUNT(s) + 1 FROM Servicio s WHERE s.numeroServicio LIKE CONCAT('SRV-', :year, '-%')")
    Long getNextSequenceForYear(@Param("year") String year);

    /**
     * Servicios con deuda mayor a un monto
     */
    @Query("SELECT s FROM Servicio s WHERE (s.montoFacturado - s.montoPagado) > :monto")
    List<Servicio> findServiciosConDeudaMayorA(@Param("monto") BigDecimal monto);

    /**
     * Estadísticas de consumo por tipo de servicio
     */
    @Query("SELECT s.tipoServicio, AVG(s.consumoActual - s.consumoAnterior), " +
           "MIN(s.consumoActual - s.consumoAnterior), MAX(s.consumoActual - s.consumoAnterior) " +
           "FROM Servicio s WHERE s.consumoActual IS NOT NULL AND s.consumoAnterior IS NOT NULL " +
           "GROUP BY s.tipoServicio")
    List<Object[]> getEstadisticasConsumo();
}
