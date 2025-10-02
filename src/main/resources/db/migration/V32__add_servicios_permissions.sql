-- Migración para agregar permisos del módulo de Servicios
-- Versión: V32
-- Descripción: Crear permisos y asignarlos a roles para el módulo de Servicios Municipales

-- Insertar permisos para el módulo de Servicios
INSERT IGNORE INTO permisos (clave, nombre) VALUES
('SERVICIOS_READ', 'Leer servicios municipales'),
('SERVICIOS_WRITE', 'Crear y editar servicios municipales'),
('SERVICIOS_GESTIONAR', 'Gestión completa de servicios municipales');

-- Asignar permisos al rol ADMIN (solo si existe)
INSERT INTO roles_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r, permisos p 
WHERE r.nombre = 'ADMIN' 
AND p.clave IN ('SERVICIOS_READ', 'SERVICIOS_WRITE', 'SERVICIOS_GESTIONAR');

-- Asignar permisos al rol OPERADOR (solo si existe)
INSERT INTO roles_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r, permisos p 
WHERE r.nombre = 'OPERADOR' 
AND p.clave IN ('SERVICIOS_READ', 'SERVICIOS_WRITE');

-- Asignar permisos al rol CONSULTA (solo si existe)
INSERT INTO roles_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r, permisos p 
WHERE r.nombre = 'CONSULTA' 
AND p.clave = 'SERVICIOS_READ';

-- Verificar que los permisos se crearon correctamente
SELECT 'Permisos de Servicios creados exitosamente' as resultado;

-- Mostrar resumen de permisos creados
SELECT 
    p.clave,
    p.nombre,
    COUNT(rp.rol_id) as roles_asignados
FROM permisos p
LEFT JOIN roles_permisos rp ON p.id = rp.permiso_id
WHERE p.clave LIKE 'SERVICIOS_%'
GROUP BY p.id, p.clave, p.nombre;
