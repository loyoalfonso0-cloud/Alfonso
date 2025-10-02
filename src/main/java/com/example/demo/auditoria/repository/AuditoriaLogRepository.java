package com.example.demo.auditoria.repository;

import com.example.demo.auditoria.model.AuditoriaLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaLogRepository extends JpaRepository<AuditoriaLog, Long> {
    
    // Buscar por usuario
    List<AuditoriaLog> findByUsuarioContainingIgnoreCaseOrderByFechaHoraDesc(String usuario);
    
    // Buscar por módulo
    List<AuditoriaLog> findByModuloOrderByFechaHoraDesc(String modulo);
    
    // Buscar por acción
    List<AuditoriaLog> findByAccionOrderByFechaHoraDesc(String accion);
    
    // Buscar por rango de fechas
    List<AuditoriaLog> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime inicio, LocalDateTime fin);
    
    // Buscar por IP
    List<AuditoriaLog> findByIpAddressOrderByFechaHoraDesc(String ipAddress);
    
    // Búsqueda general con paginación
    @Query("SELECT a FROM AuditoriaLog a WHERE " +
           "(:usuario IS NULL OR LOWER(a.usuario) LIKE LOWER(CONCAT('%', :usuario, '%'))) AND " +
           "(:modulo IS NULL OR a.modulo = :modulo) AND " +
           "(:accion IS NULL OR a.accion = :accion) AND " +
           "(:fechaInicio IS NULL OR a.fechaHora >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR a.fechaHora <= :fechaFin) AND " +
           "(:ip IS NULL OR a.ipAddress = :ip) AND " +
           "(:termino IS NULL OR LOWER(a.descripcion) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "                    LOWER(a.entidad) LIKE LOWER(CONCAT('%', :termino, '%'))) " +
           "ORDER BY a.fechaHora DESC")
    Page<AuditoriaLog> buscarConFiltros(@Param("usuario") String usuario,
                                       @Param("modulo") String modulo,
                                       @Param("accion") String accion,
                                       @Param("fechaInicio") LocalDateTime fechaInicio,
                                       @Param("fechaFin") LocalDateTime fechaFin,
                                       @Param("ip") String ip,
                                       @Param("termino") String termino,
                                       Pageable pageable);
    
    // Estadísticas de actividad por usuario
    @Query("SELECT a.usuario, COUNT(a) as total FROM AuditoriaLog a " +
           "WHERE a.fechaHora >= :fechaInicio " +
           "GROUP BY a.usuario ORDER BY total DESC")
    List<Object[]> obtenerActividadPorUsuario(@Param("fechaInicio") LocalDateTime fechaInicio);
    
    // Estadísticas de actividad por módulo
    @Query("SELECT a.modulo, COUNT(a) as total FROM AuditoriaLog a " +
           "WHERE a.fechaHora >= :fechaInicio " +
           "GROUP BY a.modulo ORDER BY total DESC")
    List<Object[]> obtenerActividadPorModulo(@Param("fechaInicio") LocalDateTime fechaInicio);
    
    // Estadísticas de actividad por acción
    @Query("SELECT a.accion, COUNT(a) as total FROM AuditoriaLog a " +
           "WHERE a.fechaHora >= :fechaInicio " +
           "GROUP BY a.accion ORDER BY total DESC")
    List<Object[]> obtenerActividadPorAccion(@Param("fechaInicio") LocalDateTime fechaInicio);
    
    // Actividad por horas del día
    @Query("SELECT HOUR(a.fechaHora) as hora, COUNT(a) as total FROM AuditoriaLog a " +
           "WHERE a.fechaHora >= :fechaInicio " +
           "GROUP BY HOUR(a.fechaHora) ORDER BY hora")
    List<Object[]> obtenerActividadPorHora(@Param("fechaInicio") LocalDateTime fechaInicio);
    
    // Logs de errores
    @Query("SELECT a FROM AuditoriaLog a WHERE a.resultado = 'ERROR' " +
           "ORDER BY a.fechaHora DESC")
    List<AuditoriaLog> obtenerLogsDeErrores();
    
    // Últimos logs por usuario
    @Query("SELECT a FROM AuditoriaLog a WHERE a.usuario = :usuario " +
           "ORDER BY a.fechaHora DESC")
    Page<AuditoriaLog> obtenerUltimosLogsPorUsuario(@Param("usuario") String usuario, Pageable pageable);
    
    // Contar logs por día
    @Query("SELECT DATE(a.fechaHora) as fecha, COUNT(a) as total FROM AuditoriaLog a " +
           "WHERE a.fechaHora >= :fechaInicio " +
           "GROUP BY DATE(a.fechaHora) ORDER BY fecha DESC")
    List<Object[]> contarLogsPorDia(@Param("fechaInicio") LocalDateTime fechaInicio);
    
    // Estadísticas generales
    @Query("SELECT COUNT(a) FROM AuditoriaLog a")
    Long contarTotalLogs();
    
    @Query("SELECT COUNT(DISTINCT a.usuario) FROM AuditoriaLog a WHERE a.fechaHora >= :fechaInicio")
    Long contarUsuariosActivos(@Param("fechaInicio") LocalDateTime fechaInicio);
    
    @Query("SELECT COUNT(a) FROM AuditoriaLog a WHERE a.fechaHora >= :fechaInicio")
    Long contarLogsRecientes(@Param("fechaInicio") LocalDateTime fechaInicio);
    
    @Query("SELECT COUNT(a) FROM AuditoriaLog a WHERE a.resultado = 'ERROR' AND a.fechaHora >= :fechaInicio")
    Long contarErroresRecientes(@Param("fechaInicio") LocalDateTime fechaInicio);
}
