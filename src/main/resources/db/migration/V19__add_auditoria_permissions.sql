-- Migración V19: Agregar permisos de auditoría
-- Fecha: 2025-01-17
-- Descripción: Agregar permisos para el módulo de auditoría y asignarlos al SUPER_ADMIN

-- Insertar permisos de auditoría
INSERT IGNORE INTO permisos (clave, nombre) VALUES
('AUDITORIA_VER', 'Ver logs de auditoría'),
('AUDITORIA_GESTIONAR', 'Gestionar logs de auditoría'),
('AUDITORIA_EXPORTAR', 'Exportar logs de auditoría'),
('AUDITORIA_ESTADISTICAS', 'Ver estadísticas de auditoría');

-- Obtener el ID del rol SUPER_ADMIN
SET @super_admin_id = (SELECT id FROM roles WHERE nombre = 'SUPER_ADMIN' LIMIT 1);

-- Asignar todos los permisos de auditoría al SUPER_ADMIN
INSERT IGNORE INTO roles_permisos (rol_id, permiso_id)
SELECT @super_admin_id, p.id 
FROM permisos p 
WHERE p.clave LIKE 'AUDITORIA_%';

-- También asignar permisos de auditoría al ADMIN_TRIBUTARIO (solo ver y estadísticas)
SET @admin_tributario_id = (SELECT id FROM roles WHERE nombre = 'ADMIN_TRIBUTARIO' LIMIT 1);

INSERT IGNORE INTO roles_permisos (rol_id, permiso_id)
SELECT @admin_tributario_id, p.id 
FROM permisos p 
WHERE p.clave IN ('AUDITORIA_VER', 'AUDITORIA_ESTADISTICAS');
