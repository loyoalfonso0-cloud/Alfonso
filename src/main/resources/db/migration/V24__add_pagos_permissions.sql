-- Agregar permisos para el módulo de Pagos
INSERT IGNORE INTO permisos (clave, nombre) VALUES
('PAGOS_READ', 'Leer información de pagos'),
('PAGOS_WRITE', 'Crear y editar pagos'),
('PAGOS_GESTIONAR', 'Gestión completa de pagos');

-- Asignar permisos de pagos al rol ADMIN_TRIBUTARIO
INSERT IGNORE INTO roles_permisos (rol_id, permiso_id)
SELECT r.id, p.id 
FROM roles r, permisos p 
WHERE r.nombre = 'ADMIN_TRIBUTARIO' 
AND p.clave IN ('PAGOS_READ', 'PAGOS_WRITE', 'PAGOS_GESTIONAR');

-- Asignar permisos de pagos al rol SUPER_ADMIN
INSERT IGNORE INTO roles_permisos (rol_id, permiso_id)
SELECT r.id, p.id 
FROM roles r, permisos p 
WHERE r.nombre = 'SUPER_ADMIN' 
AND p.clave IN ('PAGOS_READ', 'PAGOS_WRITE', 'PAGOS_GESTIONAR');

-- Asignar permisos básicos de lectura al rol USUARIO_TRIBUTARIO
INSERT IGNORE INTO roles_permisos (rol_id, permiso_id)
SELECT r.id, p.id 
FROM roles r, permisos p 
WHERE r.nombre = 'USUARIO_TRIBUTARIO' 
AND p.clave = 'PAGOS_READ';
