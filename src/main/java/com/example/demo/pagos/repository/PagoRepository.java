package com.example.demo.pagos.repository;

import com.example.demo.pagos.model.EstadoPago;
import com.example.demo.pagos.model.MetodoPago;
import com.example.demo.pagos.model.Pago;
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
public interface PagoRepository extends JpaRepository<Pago, Long> {

    // Buscar pagos por contribuyente
    List<Pago> findByContribuyenteIdOrderByFechaPagoDesc(Long contribuyenteId);
    
    Page<Pago> findByContribuyenteIdOrderByFechaPagoDesc(Long contribuyenteId, Pageable pageable);

    // Buscar pagos por declaración
    List<Pago> findByDeclaracionIdOrderByFechaPagoDesc(Long declaracionId);

    // Buscar pagos por estado
    List<Pago> findByEstadoOrderByFechaPagoDesc(EstadoPago estado);
    
    Page<Pago> findByEstadoOrderByFechaPagoDesc(EstadoPago estado, Pageable pageable);

    // Buscar pagos por método de pago
    List<Pago> findByMetodoPagoOrderByFechaPagoDesc(MetodoPago metodoPago);

    // Buscar pagos por referencia
    Optional<Pago> findByReferencia(String referencia);

    // Buscar pagos en un rango de fechas
    @Query("SELECT p FROM Pago p WHERE p.fechaPago BETWEEN :fechaInicio AND :fechaFin ORDER BY p.fechaPago DESC")
    List<Pago> findByFechaPagoBetween(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                     @Param("fechaFin") LocalDateTime fechaFin);

    // Buscar pagos por contribuyente y estado
    List<Pago> findByContribuyenteIdAndEstadoOrderByFechaPagoDesc(Long contribuyenteId, EstadoPago estado);

    // Sumar montos por contribuyente
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.contribuyente.id = :contribuyenteId AND p.estado = :estado")
    BigDecimal sumMontoByContribuyenteAndEstado(@Param("contribuyenteId") Long contribuyenteId, 
                                               @Param("estado") EstadoPago estado);

    // Contar pagos por estado
    long countByEstado(EstadoPago estado);

    // Obtener pagos recientes
    @Query("SELECT p FROM Pago p ORDER BY p.creadoEn DESC")
    List<Pago> findRecentPayments(Pageable pageable);

    // Buscar pagos por concepto (búsqueda parcial)
    @Query("SELECT p FROM Pago p WHERE LOWER(p.concepto) LIKE LOWER(CONCAT('%', :concepto, '%')) ORDER BY p.fechaPago DESC")
    List<Pago> findByConceptoContainingIgnoreCase(@Param("concepto") String concepto);

    // Estadísticas de pagos por método
    @Query("SELECT p.metodoPago, COUNT(p), SUM(p.monto) FROM Pago p WHERE p.estado = :estado GROUP BY p.metodoPago")
    List<Object[]> getEstadisticasByMetodoPago(@Param("estado") EstadoPago estado);

    // Pagos del día actual
    @Query("SELECT p FROM Pago p WHERE DATE(p.fechaPago) = CURRENT_DATE ORDER BY p.fechaPago DESC")
    List<Pago> findPagosDelDia();

    // Verificar si existe pago con referencia para evitar duplicados
    boolean existsByReferenciaAndEstadoNot(String referencia, EstadoPago estado);
}
