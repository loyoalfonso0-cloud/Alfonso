-- Migración V17: Crear tabla de auditoría/logs del sistema
-- Fecha: 2025-01-17
-- Descripción: Tabla para registrar todas las acciones realizadas en el sistema

CREATE TABLE IF NOT EXISTS auditoria_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(100) NOT NULL COMMENT 'Usuario que realizó la acción',
    accion VARCHAR(50) NOT NULL COMMENT 'Tipo de acción (CREATE, UPDATE, DELETE, LOGIN, etc.)',
    modulo VARCHAR(50) NOT NULL COMMENT 'Módulo del sistema (CONTRIBUYENTES, RETENCIONES, etc.)',
    entidad VARCHAR(50) NULL COMMENT 'Nombre de la entidad afectada',
    entidad_id BIGINT NULL COMMENT 'ID del registro afectado',
    descripcion TEXT NULL COMMENT 'Descripción detallada de la acción',
    valores_anteriores TEXT NULL COMMENT 'Valores antes del cambio (JSON)',
    valores_nuevos TEXT NULL COMMENT 'Valores después del cambio (JSON)',
    ip_address VARCHAR(45) NULL COMMENT 'Dirección IP del usuario (IPv4 o IPv6)',
    user_agent VARCHAR(500) NULL COMMENT 'Información del navegador/cliente',
    session_id VARCHAR(100) NULL COMMENT 'ID de la sesión',
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha y hora de la acción',
    resultado VARCHAR(20) NULL DEFAULT 'SUCCESS' COMMENT 'Resultado de la acción (SUCCESS, ERROR, etc.)',
    mensaje_error TEXT NULL COMMENT 'Mensaje de error si la acción falló',
    
    -- Índices para optimizar consultas
    INDEX idx_usuario (usuario),
    INDEX idx_accion (accion),
    INDEX idx_modulo (modulo),
    INDEX idx_fecha_hora (fecha_hora),
    INDEX idx_entidad (entidad, entidad_id),
    INDEX idx_ip_address (ip_address),
    INDEX idx_resultado (resultado),
    INDEX idx_usuario_fecha (usuario, fecha_hora),
    INDEX idx_modulo_fecha (modulo, fecha_hora)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Tabla de auditoría para registrar todas las acciones del sistema';

-- Insertar log inicial del sistema si no existe
INSERT IGNORE INTO auditoria_logs (usuario, accion, modulo, descripcion, ip_address, resultado) 
VALUES ('SISTEMA', 'CREATE', 'AUDITORIA', 'Tabla de auditoría creada exitosamente', '127.0.0.1', 'SUCCESS');
